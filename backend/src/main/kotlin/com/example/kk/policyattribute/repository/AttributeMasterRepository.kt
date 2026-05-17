package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AttributeMasterRepository : JpaRepository<AttributeMaster, String> {

    fun findByStatus(status: AttributeStatus): List<AttributeMaster>

    @Query(
        "SELECT a FROM AttributeMaster a WHERE " +
        "(:status IS NULL OR a.status = :status) AND " +
        "(LOWER(a.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        " LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    fun searchAttributes(
        @Param("search") search: String,
        @Param("status") status: AttributeStatus?
    ): List<AttributeMaster>

    @Query(
        "SELECT a FROM AttributeMaster a WHERE " +
        "LOWER(a.displayName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%'))"
    )
    fun searchByNameOrCode(@Param("search") search: String): List<AttributeMaster>
}
