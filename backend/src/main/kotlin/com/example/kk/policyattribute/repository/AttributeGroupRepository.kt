package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttributeGroupRepository : JpaRepository<AttributeGroup, String> {

    /**
     * Find groups by status ordered by display_order ascending.
     */
    fun findByStatusOrderByDisplayOrderAsc(status: AttributeStatus): List<AttributeGroup>

    /**
     * Find all groups ordered by display_order ascending.
     */
    fun findAllByOrderByDisplayOrderAsc(): List<AttributeGroup>
}
