package com.example.kk.policyattribute.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

/**
 * Master entity for Policies.
 */
@Table("policy_master")
class PolicyMaster(

    @Id
    @Column("policy_no")
    var policyNo: String = "",

    @Column("status")
    var status: String = "ACTIVE",

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
    override fun getId(): String = policyNo

    @Transient
    private var isNewEntity: Boolean = false

    @JsonIgnore
    override fun isNew(): Boolean = isNewEntity || createdAt == null

    fun setNew(isNew: Boolean) {
        this.isNewEntity = isNew
    }
}
