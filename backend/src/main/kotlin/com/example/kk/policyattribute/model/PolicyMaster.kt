package com.example.kk.policyattribute.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * Master entity for Policies.
 */
@Entity
@Table(name = "policy_master")
@EntityListeners(AuditingEntityListener::class)
class PolicyMaster(

    @Id
    @Column(name = "policy_no", length = 50)
    var policyNo: String = "",

    @Column(name = "status", length = 20, nullable = false)
    var status: String = "ACTIVE",

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
