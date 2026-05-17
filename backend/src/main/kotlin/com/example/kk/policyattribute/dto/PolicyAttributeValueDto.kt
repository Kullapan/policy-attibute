package com.example.kk.policyattribute.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant

/**
 * DTO for Policy Attribute Value operations.
 */
data class PolicyAttributeValueDto(

    @field:NotBlank(message = "Policy number is required")
    val policyNo: String = "",

    @field:NotBlank(message = "Attribute code is required")
    val attributeCode: String = "",

    val attributeValue: String? = null,

    /** Attribute metadata (read-only, populated on GET) */
    val displayName: String? = null,
    val dataType: String? = null,
    val isRequired: Boolean = false,
    val regexPattern: String? = null,
    val regexErrorMsg: String? = null,

    // Read-only audit fields
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val createdBy: String? = null
)
