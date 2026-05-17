package com.example.kk.policyattribute.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

/**
 * Composite primary key for policy_attribute_values table.
 * Composed of (policy_no, attribute_code).
 * Implements Serializable and equals/hashCode as required by JPA for embedded IDs.
 */
@Embeddable
data class PolicyAttributeValueId(

    @Column(name = "policy_no", length = 50, nullable = false)
    val policyNo: String = "",

    @Column(name = "attribute_code", length = 100, nullable = false)
    val attributeCode: String = ""

) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
