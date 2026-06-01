package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeStatus
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository

@Repository
interface AttributeGroupRepository : CoroutineCrudRepository<AttributeGroup, String> {

    /**
     * Find groups by status ordered by display_order ascending.
     */
    fun findByStatusOrderByDisplayOrderAsc(status: AttributeStatus): Flow<AttributeGroup>

    /**
     * Find all groups ordered by display_order ascending.
     */
    fun findAllByOrderByDisplayOrderAsc(): Flow<AttributeGroup>
}
