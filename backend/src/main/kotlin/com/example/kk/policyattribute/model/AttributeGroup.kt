package com.example.kk.policyattribute.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

/**
 * Entity representing an attribute category/group.
 * Used to dynamically group attributes and render tabs in the UI.
 */
@Table("attribute_group")
class AttributeGroup(

    @Id
    @Column("code")
    var code: String = "",

    @Column("display_name_en")
    var displayNameEn: String = "",

    @Column("display_name_th")
    var displayNameTh: String = "",

    @Column("display_order")
    var displayOrder: Int = 0,

    @Column("status")
    var status: AttributeStatus = AttributeStatus.ACTIVE,

    @Version
    @Column("version")
    var version: Long? = null,

    @CreatedDate
    @Column("created_at")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column("updated_at")
    var updatedAt: Instant? = null,

    @CreatedBy
    @Column("created_by")
    var createdBy: String? = null

) : Persistable<String> {

    @JsonIgnore
    override fun getId(): String = code

    @Transient
    private var isNewEntity: Boolean = false

    @JsonIgnore
    override fun isNew(): Boolean = isNewEntity || version == null

    fun setNew(isNew: Boolean) {
        this.isNewEntity = isNew
    }
}
