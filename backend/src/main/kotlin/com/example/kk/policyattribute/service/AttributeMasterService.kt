package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.AttributeGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.regex.PatternSyntaxException

@Service
class AttributeMasterService(
    private val repository: AttributeMasterRepository,
    private val groupRepository: AttributeGroupRepository
) {

    /**
     * List all attributes, optionally filtered by status and/or search term.
     */
    @Transactional(readOnly = true)
    fun listAttributes(search: String?, status: AttributeStatus?): List<AttributeMasterDto> {
        val results = when {
            !search.isNullOrBlank() && status != null -> repository.searchAttributes(search.trim(), status)
            !search.isNullOrBlank()                   -> repository.searchByNameOrCode(search.trim())
            status != null                            -> repository.findByStatus(status)
            else                                      -> repository.findAll()
        }
        return results.map { toDto(it) }
    }

    /**
     * Get a single attribute by code.
     */
    @Transactional(readOnly = true)
    fun getByCode(code: String): AttributeMasterDto = toDto(findByCodeOrThrow(code))

    /**
     * Create a new attribute definition.
     */
    @Transactional
    fun create(dto: AttributeMasterDto): AttributeMasterDto {
        if (repository.existsById(dto.code)) {
            throw AttributeValidationException(dto.code, "Attribute with code '${dto.code}' already exists")
        }
        validateRegexPattern(dto)
        val entity = toEntity(dto).apply { status = AttributeStatus.ACTIVE }
        return toDto(repository.save(entity))
    }

    /**
     * Update an existing attribute definition.
     */
    @Transactional
    fun update(code: String, dto: AttributeMasterDto): AttributeMasterDto {
        val existing = findByCodeOrThrow(code)
        validateRegexPattern(dto)

        existing.displayName = dto.displayName
        existing.dataType = dto.dataType ?: existing.dataType
        existing.isRequired = dto.isRequired
        existing.regexPattern = dto.regexPattern
        existing.regexErrorMsg = dto.regexErrorMsg
        existing.attributeGroup = dto.groupCode?.let { gc ->
            if (gc.isNotBlank()) {
                groupRepository.findById(gc)
                    .orElseThrow { ResourceNotFoundException("AttributeGroup", gc) }
            } else null
        }

        // Optimistic locking via @Version — pass version from client
        dto.version?.let { existing.version = it }

        return toDto(repository.save(existing))
    }

    /**
     * Soft-delete: set status to ARCHIVED.
     */
    @Transactional
    fun softDelete(code: String) {
        val entity = findByCodeOrThrow(code)
        entity.status = AttributeStatus.ARCHIVED
        repository.save(entity)
    }

    // ── Private helpers ──────────────────────────────────────

    private fun findByCodeOrThrow(code: String): AttributeMaster =
        repository.findById(code)
            .orElseThrow { ResourceNotFoundException("AttributeMaster", code) }

    private fun validateRegexPattern(dto: AttributeMasterDto) {
        val pattern = dto.regexPattern ?: return
        if (pattern.isBlank()) return
        try {
            java.util.regex.Pattern.compile(pattern)
        } catch (e: PatternSyntaxException) {
            throw AttributeValidationException(dto.code, "Invalid regex pattern: ${e.description}")
        }
    }

    private fun toDto(entity: AttributeMaster) = AttributeMasterDto(
        code          = entity.code,
        displayName   = entity.displayName,
        dataType      = entity.dataType,
        status        = entity.status,
        isRequired    = entity.isRequired,
        regexPattern  = entity.regexPattern,
        regexErrorMsg = entity.regexErrorMsg,
        groupCode     = entity.attributeGroup?.code,
        version       = entity.version,
        createdAt     = entity.createdAt,
        updatedAt     = entity.updatedAt,
        createdBy     = entity.createdBy
    )

    private fun toEntity(dto: AttributeMasterDto) = AttributeMaster(
        code          = dto.code,
        displayName   = dto.displayName,
        dataType      = dto.dataType ?: com.example.kk.policyattribute.model.DataType.STRING,
        isRequired    = dto.isRequired,
        regexPattern  = dto.regexPattern,
        regexErrorMsg = dto.regexErrorMsg
    ).apply {
        attributeGroup = dto.groupCode?.let { gc ->
            if (gc.isNotBlank()) {
                groupRepository.findById(gc)
                    .orElseThrow { ResourceNotFoundException("AttributeGroup", gc) }
            } else null
        }
    }
}
