package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.PolicyMaster
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for PolicyMaster.
 */
@Repository
interface PolicyMasterRepository : JpaRepository<PolicyMaster, String>
