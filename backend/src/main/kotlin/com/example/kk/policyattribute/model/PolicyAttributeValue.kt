package com.example.kk.policyattribute.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * Mapping entity that associates an attribute value with a specific policy.
 * Uses a composite PK of (policy_no, attribute_code).
 */
@Entity
@Table(name = "policy_attribute_values")
@EntityListeners(AuditingEntityListener::class)
class PolicyAttributeValue(

    @EmbeddedId
    var id: PolicyAttributeValueId = PolicyAttributeValueId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_no", referencedColumnName = "policy_no",
        insertable = false, updatable = false)
    var policyMaster: PolicyMaster? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_code", referencedColumnName = "code",
        insertable = false, updatable = false)
    var attributeMaster: AttributeMaster? = null,

    @Column(name = "attribute_value", columnDefinition = "TEXT")
    var attributeValue: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,

    @CreatedBy
    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    var createdBy: String? = null
)
