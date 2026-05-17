package com.example.kk.policyattribute.dto

import jakarta.validation.constraints.Size

/**
 * Strongly-typed request DTO for updating a single policy attribute value.
 * Replaces the previous loose Map<String, String> body to enable declarative
 * JSR-380 validation and proper OpenAPI documentation.
 */
data class UpdateAttributeValueRequestDto(
    @field:Size(max = 2000, message = "Attribute value must not exceed 2000 characters")
    val attributeValue: String? = null
)
