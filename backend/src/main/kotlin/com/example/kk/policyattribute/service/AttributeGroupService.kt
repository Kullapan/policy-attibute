package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.AttributeGroupDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.repository.AttributeGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttributeGroupService(
    private val repository: AttributeGroupRepository
) {

    /**
     * Lists active attribute groups ordered by displayOrder.
     */
    @Transactional(readOnly = true)
    fun listActiveGroups(): Flow<AttributeGroupDto> {
        val groups = repository.findByStatusOrderByDisplayOrderAsc(AttributeStatus.ACTIVE)
        return groups.map { toDto(it) }
    }

    /**
     * Lists attribute groups ordered by displayOrder, optionally including archived.
     */
    @Transactional(readOnly = true)
    fun listGroups(includeArchived: Boolean): Flow<AttributeGroupDto> {
        val groups = if (includeArchived) {
            repository.findAllByOrderByDisplayOrderAsc()
        } else {
            repository.findByStatusOrderByDisplayOrderAsc(AttributeStatus.ACTIVE)
        }
        return groups.map { toDto(it) }
    }

    /**
     * Get a single group by code.
     */
    @Transactional(readOnly = true)
    suspend fun getByCode(code: String): AttributeGroupDto = toDto(findByCodeOrThrow(code))

    /**
     * Create a new attribute group.
     */
    @Transactional
    suspend fun create(dto: AttributeGroupDto): AttributeGroupDto {
        if (repository.existsById(dto.code)) {
            throw AttributeValidationException("Group code '${dto.code}' already exists")
        }
        val entity = toEntity(dto).apply {
            status = AttributeStatus.ACTIVE
            setNew(true)
        }
        return toDto(repository.save(entity))
    }

    /**
     * Update an existing attribute group.
     */
    @Transactional
    suspend fun update(code: String, dto: AttributeGroupDto): AttributeGroupDto {
        val existing = findByCodeOrThrow(code)

        existing.displayNameEn = dto.displayNameEn
        existing.displayNameTh = dto.displayNameTh
        existing.displayOrder = dto.displayOrder
        dto.status?.let { existing.status = it }

        // Optimistic locking via @Version
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

    private suspend fun findByCodeOrThrow(code: String): AttributeGroup =
        repository.findById(code)
            ?: throw ResourceNotFoundException("AttributeGroup", code)

    /**
     * Helper to map Entity to DTO.
     */
    fun toDto(entity: AttributeGroup) = AttributeGroupDto(
        code = entity.code,
        displayNameEn = entity.displayNameEn,
        displayNameTh = entity.displayNameTh,
        displayOrder = entity.displayOrder,
        status = entity.status,
        version = entity.version,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        createdBy = entity.createdBy
    )

    private fun toEntity(dto: AttributeGroupDto) = AttributeGroup(
        code = dto.code,
        displayNameEn = dto.displayNameEn,
        displayNameTh = dto.displayNameTh,
        displayOrder = dto.displayOrder
    )
}
