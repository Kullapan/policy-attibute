package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.PolicyAttributeValue
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Repository

@Repository
interface PolicyAttributeValueRepository : CoroutineCrudRepository<PolicyAttributeValue, Long> {

    /**
     * Find values associated with a specific policy number.
     */
    fun findByPolicyNo(policyNo: String): Flow<PolicyAttributeValue>

    /**
     * Find a specific attribute value for a policy.
     */
    suspend fun findByPolicyNoAndAttributeCode(policyNo: String, attributeCode: String): PolicyAttributeValue?

    /**
     * Delete a specific attribute value for a policy.
     */
    suspend fun deleteByPolicyNoAndAttributeCode(policyNo: String, attributeCode: String)
}
