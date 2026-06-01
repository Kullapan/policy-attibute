package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.AttributeGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    fun listAttributes(search: String?, status: AttributeStatus?): Flow<AttributeMasterDto> {
        val results = when {
            !search.isNullOrBlank() && status != null -> repository.searchAttributes(search.trim(), status.name)
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
    suspend fun getByCode(code: String): AttributeMasterDto = toDto(findByCodeOrThrow(code))

    /**
     * Create a new attribute definition.
     */
    @Transactional
    suspend fun create(dto: AttributeMasterDto): AttributeMasterDto {
        if (repository.existsById(dto.code)) {
            throw AttributeValidationException(dto.code, "Attribute with code '${dto.code}' already exists")
        }
        validateRegexPattern(dto)
        validateGroupCode(dto.groupCode)
        val entity = toEntity(dto).apply {
            status = AttributeStatus.ACTIVE
            setNew(true)
        }
        return toDto(repository.save(entity))
    }

    /**
     * Update an existing attribute definition.
     */
    @Transactional
    suspend fun update(code: String, dto: AttributeMasterDto): AttributeMasterDto {
        val existing = findByCodeOrThrow(code)
        validateRegexPattern(dto)
        validateGroupCode(dto.groupCode)

        existing.displayName = dto.displayName
        existing.dataType = dto.dataType ?: existing.dataType
        existing.isRequired = dto.isRequired
        existing.regexPattern = dto.regexPattern
        existing.regexErrorMsg = dto.regexErrorMsg
        existing.groupCode = dto.groupCode

        // Optimistic locking via @Version — pass version from client
        dto.version?.let { existing.version = it }

        return toDto(repository.save(existing))
    }

    /**
     * Soft-delete: set status to ARCHIVED.
     */
    @Transactional
    suspend fun softDelete(code: String) {
        val entity = findByCodeOrThrow(code)
        entity.status = AttributeStatus.ARCHIVED
        repository.save(entity)
    }

    // ── Private helpers ──────────────────────────────────────

    private suspend fun findByCodeOrThrow(code: String): AttributeMaster =
        repository.findById(code)
            ?: throw ResourceNotFoundException("AttributeMaster", code)

    private fun validateRegexPattern(dto: AttributeMasterDto) {
        val pattern = dto.regexPattern ?: return
        if (pattern.isBlank()) return
        try {
            java.util.regex.Pattern.compile(pattern)
        } catch (e: PatternSyntaxException) {
            throw AttributeValidationException(dto.code, "Invalid regex pattern: ${e.description}")
        }
    }

    private suspend fun validateGroupCode(groupCode: String?) {
        if (!groupCode.isNullOrBlank()) {
            if (!groupRepository.existsById(groupCode)) {
                throw ResourceNotFoundException("AttributeGroup", groupCode)
            }
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
        groupCode     = entity.groupCode,
        version       = entity.version,
        createdAt     = entity.createdAt,
        updatedAt     = entity.updatedAt,
        createdBy     = entity.createdBy
    )

    private fun toEntity(dto: AttributeMasterDto) = AttributeMaster(
        code          = dto.code,
        displayName   = dto.displayName,
        dataType      = dto.dataType ?: DataType.STRING,
        isRequired    = dto.isRequired,
        regexPattern  = dto.regexPattern,
        regexErrorMsg = dto.regexErrorMsg,
        groupCode     = dto.groupCode
    )
}
