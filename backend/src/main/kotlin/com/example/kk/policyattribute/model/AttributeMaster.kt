package com.example.kk.policyattribute.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * Master dictionary entity for policy attribute definitions.
 * Uses soft-delete (status → ARCHIVED) and optimistic locking (@Version).
 */
@Entity
@Table(name = "attribute_master")
@EntityListeners(AuditingEntityListener::class)
class AttributeMaster(

    @Id
    @Column(name = "code", length = 100, nullable = false, updatable = false)
    var code: String = "",

    @Column(name = "display_name", length = 255, nullable = false)
    var displayName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20, nullable = false)
    var dataType: DataType = DataType.STRING,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: AttributeStatus = AttributeStatus.ACTIVE,

    @Column(name = "is_required", nullable = false)
    var isRequired: Boolean = false,

    @Column(name = "regex_pattern", length = 255)
    var regexPattern: String? = null,

    @Column(name = "regex_error_msg", length = 255)
    var regexErrorMsg: String? = null,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long? = null,

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
