package com.example.kk.policyattribute.dto

import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.Instant

/**
 * DTO for Attribute Master CRUD operations.
 */
data class AttributeMasterDto(

    @field:NotBlank(message = "Attribute code is required")
    @field:Pattern(
        regexp = "^[A-Z][A-Z0-9_]*$",
        message = "Code must be UPPER_SNAKE_CASE (start with letter, only A-Z, 0-9, _)"
    )
    val code: String = "",

    @field:NotBlank(message = "Display name is required")
    val displayName: String = "",

    @field:NotNull(message = "Data type is required")
    val dataType: DataType? = null,

    val status: AttributeStatus? = null,

    val isRequired: Boolean = false,

    val regexPattern: String? = null,

    val regexErrorMsg: String? = null,

    val version: Long? = null,

    // Read-only audit fields
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val createdBy: String? = null
)
