package com.example.kk.policyattribute.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * Entity representing an attribute category/group.
 * Used to dynamically group attributes and render tabs in the UI.
 */
@Entity
@Table(name = "attribute_group")
@EntityListeners(AuditingEntityListener::class)
class AttributeGroup(

    @Id
    @Column(name = "code", length = 50, nullable = false)
    var code: String = "",

    @Column(name = "display_name_en", length = 100, nullable = false)
    var displayNameEn: String = "",

    @Column(name = "display_name_th", length = 100, nullable = false)
    var displayNameTh: String = "",

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: AttributeStatus = AttributeStatus.ACTIVE,

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
