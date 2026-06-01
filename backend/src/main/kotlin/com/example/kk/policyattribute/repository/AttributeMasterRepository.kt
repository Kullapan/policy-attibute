package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository

@Repository
interface AttributeMasterRepository : CoroutineCrudRepository<AttributeMaster, String> {

    /**
     * Find attributes by status.
     */
    fun findByStatus(status: AttributeStatus): Flow<AttributeMaster>

    /**
     * Search attributes by search keyword and status.
     */
    @Query(
        """
        SELECT * FROM attribute_master WHERE 
        (:status IS NULL OR status = :status) AND 
        (LOWER(display_name) LIKE LOWER(CONCAT('%', :search, '%')) OR 
         LOWER(code) LIKE LOWER(CONCAT('%', :search, '%')))
        """
    )
    fun searchAttributes(
        search: String,
        status: String?
    ): Flow<AttributeMaster>

    /**
     * Search attributes by name or code.
     */
    @Query(
        """
        SELECT * FROM attribute_master WHERE 
        LOWER(display_name) LIKE LOWER(CONCAT('%', :search, '%')) OR 
        LOWER(code) LIKE LOWER(CONCAT('%', :search, '%'))
        """
    )
    fun searchByNameOrCode(search: String): Flow<AttributeMaster>
}
