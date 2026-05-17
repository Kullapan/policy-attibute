package com.example.kk.policyattribute.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * DTO for creating a new policy and assigning attributes at the same time.
 */
data class CreatePolicyRequestDto(
    @field:NotBlank(message = "Policy number is required")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\-]+$",
        message = "Policy number must contain only alphanumeric characters and hyphens"
    )
    val policyNo: String = "",

    @field:Valid
    val attributes: List<PolicyAttributeValueDto>? = null
)

