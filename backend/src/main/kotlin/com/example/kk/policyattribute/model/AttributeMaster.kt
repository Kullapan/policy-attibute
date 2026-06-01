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
 * Master dictionary entity for policy attribute definitions.
 * Uses soft-delete (status → ARCHIVED) and optimistic locking (@Version).
 */
@Table("attribute_master")
class AttributeMaster(

    @Id
    @Column("code")
    var code: String = "",

    @Column("display_name")
    var displayName: String = "",

    @Column("data_type")
    var dataType: DataType = DataType.STRING,

    @Column("status")
    var status: AttributeStatus = AttributeStatus.ACTIVE,

    @Column("is_required")
    var isRequired: Boolean = false,

    @Column("regex_pattern")
    var regexPattern: String? = null,

    @Column("regex_error_msg")
    var regexErrorMsg: String? = null,

    @Column("group_code")
    var groupCode: String? = null,

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
