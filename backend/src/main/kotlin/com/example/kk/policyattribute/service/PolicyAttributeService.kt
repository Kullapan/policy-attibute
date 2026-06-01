package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.CreatePolicyRequestDto
import com.example.kk.policyattribute.dto.PolicyAttributeValueDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.*
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.PolicyAttributeValueRepository
import com.example.kk.policyattribute.repository.PolicyMasterRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Collections
import java.util.LinkedHashMap
import java.util.regex.Pattern

/**
 * Service for managing policy-attribute value mappings.
 * Includes Strategy-pattern data-type validation and memoized regex validation.
 */
@Service
class PolicyAttributeService(
    private val valueRepository: PolicyAttributeValueRepository,
    private val masterRepository: AttributeMasterRepository,
    private val policyMasterRepository: PolicyMasterRepository
) {
    private val log = LoggerFactory.getLogger(PolicyAttributeService::class.java)

    /**
     * Bounded LRU regex pattern cache (max 500 entries) to prevent OOM from
     * unbounded growth when many unique regex patterns are stored as attributes.
     */
    private val regexCache: MutableMap<String, Pattern> = Collections.synchronizedMap(
        object : LinkedHashMap<String, Pattern>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, Pattern>?): Boolean = size > 500
        }
    )

    /**
     * Create a new policy and bulk save its attributes.
     */
    @Transactional
    suspend fun createPolicyWithAttributes(dto: CreatePolicyRequestDto): List<PolicyAttributeValueDto> {
        if (policyMasterRepository.existsById(dto.policyNo)) {
            throw IllegalArgumentException("Policy already exists: ${dto.policyNo}")
        }
        val policy = PolicyMaster(policyNo = dto.policyNo, status = "ACTIVE").apply { setNew(true) }
        policyMasterRepository.save(policy)

        val attrs = dto.attributes
        return if (!attrs.isNullOrEmpty()) {
            bulkSaveForPolicy(dto.policyNo, attrs)
        } else {
            emptyList()
        }
    }

    /**
     * Get all policies.
     */
    @Transactional(readOnly = true)
    fun getAllPolicies(): Flow<PolicyMaster> = policyMasterRepository.findAll()

    /**
     * Get all attribute values for a given policy number.
     */
    @Transactional(readOnly = true)
    suspend fun getAttributesForPolicy(policyNo: String): List<PolicyAttributeValueDto> {
        val values = valueRepository.findByPolicyNo(policyNo).toList()
        val masters = masterRepository.findAll().toList().associateBy { it.id }

        return values.map { value ->
            val m = masters[value.attributeCode]
            PolicyAttributeValueDto(
                policyNo       = value.policyNo,
                attributeCode  = value.attributeCode,
                attributeValue = value.attributeValue,
                createdAt      = value.createdAt,
                updatedAt      = value.updatedAt,
                createdBy      = value.createdBy,
                displayName    = m?.displayName,
                dataType       = m?.dataType?.name,
                isRequired     = m?.isRequired ?: false,
                regexPattern   = m?.regexPattern,
                regexErrorMsg  = m?.regexErrorMsg,
                groupCode      = m?.groupCode
            )
        }
    }

    /**
     * Update (or create) a single attribute value for a policy.
     */
    @Transactional
    suspend fun updateAttributeValue(policyNo: String, attributeCode: String, attributeValue: String?): PolicyAttributeValueDto {
        val master = masterRepository.findById(attributeCode)
            ?: throw ResourceNotFoundException("AttributeMaster", attributeCode)

        if (master.status == AttributeStatus.ARCHIVED) {
            throw AttributeValidationException(attributeCode, "Cannot assign values to ARCHIVED attribute")
        }

        // ── Strategy-pattern data-type validation ──
        validateDataType(master, attributeValue)

        // ── Regex validation with memoization ──
        validateValueAgainstRegex(master, attributeValue)

        val existing = valueRepository.findByPolicyNoAndAttributeCode(policyNo, attributeCode)
        val entity = if (existing != null) {
            existing.apply { this.attributeValue = attributeValue }
        } else {
            PolicyAttributeValue(
                policyNo = policyNo,
                attributeCode = attributeCode,
                attributeValue = attributeValue
            )
        }

        val saved = valueRepository.save(entity)
        return PolicyAttributeValueDto(
            policyNo       = saved.policyNo,
            attributeCode  = saved.attributeCode,
            attributeValue = saved.attributeValue,
            createdAt      = saved.createdAt,
            updatedAt      = saved.updatedAt,
            createdBy      = saved.createdBy,
            displayName    = master.displayName,
            dataType       = master.dataType.name,
            isRequired     = master.isRequired,
            regexPattern   = master.regexPattern,
            regexErrorMsg  = master.regexErrorMsg,
            groupCode      = master.groupCode
        )
    }

    /**
     * Bulk save multiple attribute values for a single policy.
     */
    @Transactional
    suspend fun bulkSaveForPolicy(policyNo: String, dtos: List<PolicyAttributeValueDto>): List<PolicyAttributeValueDto> =
        dtos.map { updateAttributeValue(policyNo, it.attributeCode, it.attributeValue) }

    // ── Validation: Strategy Pattern by DataType ─────────────

    /**
     * Validates the attribute value against the expected data type.
     */
    fun validateDataType(master: AttributeMaster, value: String?) {
        if (value.isNullOrBlank()) {
            if (master.isRequired) {
                throw AttributeValidationException(master.id, "Value is required but was empty")
            }
            return // nullable non-required fields are OK
        }

        when (master.dataType) {
            DataType.NUMBER -> {
                value.toDoubleOrNull()
                    ?: throw AttributeValidationException(master.id, "Expected a valid number but got: $value")
            }
            DataType.DATE -> {
                try {
                    LocalDate.parse(value)
                } catch (e: DateTimeParseException) {
                    throw AttributeValidationException(master.id, "Expected a valid date (YYYY-MM-DD) but got: $value")
                }
            }
            DataType.BOOLEAN -> {
                if (!value.equals("true", ignoreCase = true) && !value.equals("false", ignoreCase = true)) {
                    throw AttributeValidationException(master.id, "Expected true or false but got: $value")
                }
            }
            DataType.STRING -> {
                // All string values are valid at the data-type level
            }
        }
    }

    /**
     * Validates the attribute value against the regex pattern (if defined)
     * with memoized Pattern compilation for performance.
     */
    fun validateValueAgainstRegex(master: AttributeMaster, value: String?) {
        val pattern = master.regexPattern
        if (pattern.isNullOrBlank()) return
        if (value.isNullOrBlank()) return // regex validation skipped for empty values

        val compiled = regexCache.computeIfAbsent(pattern) { Pattern.compile(it) }
        if (!compiled.matcher(value).matches()) {
            val errorMsg = master.regexErrorMsg
                ?: "Value does not match expected format: $pattern"
            throw AttributeValidationException(master.id, errorMsg)
        }
    }
}
