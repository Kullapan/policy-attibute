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
    fun createPolicyWithAttributes(dto: CreatePolicyRequestDto): List<PolicyAttributeValueDto> {
        if (policyMasterRepository.existsById(dto.policyNo)) {
            throw IllegalArgumentException("Policy already exists: ${dto.policyNo}")
        }
        policyMasterRepository.save(PolicyMaster(policyNo = dto.policyNo, status = "ACTIVE"))

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
    fun getAllPolicies(): List<PolicyMaster> = policyMasterRepository.findAll()

    /**
     * Get all attribute values for a given policy number.
     */
    @Transactional(readOnly = true)
    fun getAttributesForPolicy(policyNo: String): List<PolicyAttributeValueDto> =
        valueRepository.findByIdPolicyNo(policyNo).map { toDto(it) }

    /**
     * Update (or create) a single attribute value for a policy.
     */
    @Transactional
    fun updateAttributeValue(policyNo: String, attributeCode: String, attributeValue: String?): PolicyAttributeValueDto {
        val master = masterRepository.findById(attributeCode)
            .orElseThrow { ResourceNotFoundException("AttributeMaster", attributeCode) }

        if (master.status == AttributeStatus.ARCHIVED) {
            throw AttributeValidationException(attributeCode, "Cannot assign values to ARCHIVED attribute")
        }

        // ── Strategy-pattern data-type validation ──
        validateDataType(master, attributeValue)

        // ── Regex validation with memoization ──
        validateValueAgainstRegex(master, attributeValue)

        val id = PolicyAttributeValueId(policyNo, attributeCode)
        val entity = valueRepository.findById(id)
            .orElse(PolicyAttributeValue(id = id))
        entity.attributeValue = attributeValue

        return toDto(valueRepository.save(entity))
    }

    /**
     * Bulk save multiple attribute values for a single policy.
     */
    @Transactional
    fun bulkSaveForPolicy(policyNo: String, dtos: List<PolicyAttributeValueDto>): List<PolicyAttributeValueDto> =
        dtos.map { updateAttributeValue(policyNo, it.attributeCode, it.attributeValue) }

    // ── Validation: Strategy Pattern by DataType ─────────────

    /**
     * Validates the attribute value against the expected data type.
     */
    fun validateDataType(master: AttributeMaster, value: String?) {
        if (value.isNullOrBlank()) {
            if (master.isRequired) {
                throw AttributeValidationException(master.code, "Value is required but was empty")
            }
            return // nullable non-required fields are OK
        }

        when (master.dataType) {
            DataType.NUMBER -> {
                value.toDoubleOrNull()
                    ?: throw AttributeValidationException(master.code, "Expected a valid number but got: $value")
            }
            DataType.DATE -> {
                try {
                    LocalDate.parse(value)
                } catch (e: DateTimeParseException) {
                    throw AttributeValidationException(master.code, "Expected a valid date (YYYY-MM-DD) but got: $value")
                }
            }
            DataType.BOOLEAN -> {
                if (!value.equals("true", ignoreCase = true) && !value.equals("false", ignoreCase = true)) {
                    throw AttributeValidationException(master.code, "Expected true or false but got: $value")
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
            throw AttributeValidationException(master.code, errorMsg)
        }
    }

    // ── Mapping ──────────────────────────────────────────────

    private fun toDto(entity: PolicyAttributeValue): PolicyAttributeValueDto {
        val m = entity.attributeMaster
        return PolicyAttributeValueDto(
            policyNo       = entity.id.policyNo,
            attributeCode  = entity.id.attributeCode,
            attributeValue = entity.attributeValue,
            createdAt      = entity.createdAt,
            updatedAt      = entity.updatedAt,
            createdBy      = entity.createdBy,
            displayName    = m?.displayName,
            dataType       = m?.dataType?.name,
            isRequired     = m?.isRequired ?: false,
            regexPattern   = m?.regexPattern,
            regexErrorMsg  = m?.regexErrorMsg
        )
    }
}
