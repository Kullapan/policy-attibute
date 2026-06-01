package com.example.kk.policyattribute.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

/**
 * Mapping entity that associates an attribute value with a specific policy.
 * Uses a surrogate auto-incrementing id.
 */
@Table("policy_attribute_values")
class PolicyAttributeValue(

    @Id
    @Column("id")
    var id: Long? = null,

    @Column("policy_no")
    var policyNo: String = "",

    @Column("attribute_code")
    var attributeCode: String = "",

    @Column("attribute_value")
    var attributeValue: String? = null,

    @CreatedDate
    @Column("created_at")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column("updated_at")
    var updatedAt: Instant? = null,

    @CreatedBy
    @Column("created_by")
    var createdBy: String? = null
)
