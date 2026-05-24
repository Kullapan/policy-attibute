package com.example.kk.policyattribute.dto

import com.example.kk.policyattribute.model.AttributeStatus
import java.time.Instant

/**
 * DTO for Attribute Group.
 */
data class AttributeGroupDto(
    val code: String = "",
    val displayNameEn: String = "",
    val displayNameTh: String = "",
    val displayOrder: Int = 0,
    val status: AttributeStatus? = null,
    val version: Long? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val createdBy: String? = null
)
