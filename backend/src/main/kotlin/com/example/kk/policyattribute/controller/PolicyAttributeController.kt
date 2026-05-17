package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.CreatePolicyRequestDto
import com.example.kk.policyattribute.dto.PolicyAttributeValueDto
import com.example.kk.policyattribute.dto.UpdateAttributeValueRequestDto
import com.example.kk.policyattribute.model.PolicyMaster
import com.example.kk.policyattribute.service.PolicyAttributeService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Policy-Attribute value mapping operations.
 */
@RestController
@RequestMapping("/api/v1/policies")
class PolicyAttributeController(
    private val service: PolicyAttributeService
) {

    /**
     * Create a new policy with optional initial attributes.
     */
    @PostMapping("/create")
    fun createPolicy(@Valid @RequestBody request: CreatePolicyRequestDto): ResponseEntity<List<PolicyAttributeValueDto>> =
        ResponseEntity.ok(service.createPolicyWithAttributes(request))

    /**
     * Get all attribute values for a specific policy.
     */
    @GetMapping("/{policyNo}/attributes")
    fun getAttributesForPolicy(@PathVariable policyNo: String): ResponseEntity<List<PolicyAttributeValueDto>> =
        ResponseEntity.ok(service.getAttributesForPolicy(policyNo))

    /**
     * Update a single attribute value for a policy.
     * Request body: { "attributeValue": "..." }
     */
    @PutMapping("/{policyNo}/attributes/{attributeCode}")
    fun updateValue(
        @PathVariable policyNo: String,
        @PathVariable attributeCode: String,
        @Valid @RequestBody request: UpdateAttributeValueRequestDto
    ): ResponseEntity<PolicyAttributeValueDto> =
        ResponseEntity.ok(service.updateAttributeValue(policyNo, attributeCode, request.attributeValue))

    /**
     * Get all policies.
     */
    @GetMapping("")
    fun getAllPolicies(): ResponseEntity<List<PolicyMaster>> =
        ResponseEntity.ok(service.getAllPolicies())

    /**
     * Bulk save attribute values for a policy.
     */
    @PostMapping("/{policyNo}/attributes")
    fun bulkSave(
        @PathVariable policyNo: String,
        @Valid @RequestBody dtos: List<PolicyAttributeValueDto>
    ): ResponseEntity<List<PolicyAttributeValueDto>> =
        ResponseEntity.ok(service.bulkSaveForPolicy(policyNo, dtos))
}
