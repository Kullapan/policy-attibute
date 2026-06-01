package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.PolicyMaster
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data R2DBC repository for PolicyMaster.
 */
@Repository
interface PolicyMasterRepository : CoroutineCrudRepository<PolicyMaster, String>
