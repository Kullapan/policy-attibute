package com.example.kk.policyattribute.repository

import com.example.kk.policyattribute.model.PolicyAttributeValue
import com.example.kk.policyattribute.model.PolicyAttributeValueId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PolicyAttributeValueRepository :
    JpaRepository<PolicyAttributeValue, PolicyAttributeValueId> {

    fun findByIdPolicyNo(policyNo: String): List<PolicyAttributeValue>

    @Transactional
    fun deleteByIdPolicyNoAndIdAttributeCode(policyNo: String, attributeCode: String)
}
