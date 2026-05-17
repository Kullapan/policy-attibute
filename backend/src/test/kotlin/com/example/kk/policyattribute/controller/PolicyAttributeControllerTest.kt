package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.CreatePolicyRequestDto
import com.example.kk.policyattribute.dto.PolicyAttributeValueDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.PolicyMaster
import com.example.kk.policyattribute.service.PolicyAttributeService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant

@WebMvcTest(PolicyAttributeController::class)
@DisplayName("PolicyAttributeController Web Layer Tests")
class PolicyAttributeControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockBean lateinit var service: PolicyAttributeService

    lateinit var attrDto: PolicyAttributeValueDto

    @BeforeEach
    fun setUp() {
        attrDto = PolicyAttributeValueDto(
            policyNo = "POL-001", attributeCode = "STR_ATTR",
            attributeValue = "hello", createdAt = Instant.now()
        )
    }

    // ── POST /api/v1/policies/create ─────────────────────────

    @Test @DisplayName("POST /create → 200 OK with attribute list")
    fun createPolicy_success() {
        val req = CreatePolicyRequestDto(policyNo = "POL-001", attributes = listOf(attrDto))
        whenever(service.createPolicyWithAttributes(any())).thenReturn(listOf(attrDto))

        mockMvc.perform(post("/api/v1/policies/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].policyNo").value("POL-001"))
    }

    @Test @DisplayName("POST /create → 400 when policy already exists (IllegalArgumentException)")
    fun createPolicy_alreadyExists() {
        val req = CreatePolicyRequestDto(policyNo = "POL-DUP")
        whenever(service.createPolicyWithAttributes(any()))
            .thenThrow(IllegalArgumentException("Policy already exists: POL-DUP"))

        mockMvc.perform(post("/api/v1/policies/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest) // correctly handled by handleIllegalArgument → 400
    }

    // ── GET /api/v1/policies ─────────────────────────────────

    @Test @DisplayName("GET /api/v1/policies → 200 OK with policy list")
    fun getAllPolicies_success() {
        val pm = PolicyMaster(policyNo = "POL-001", status = "ACTIVE")
        whenever(service.getAllPolicies()).thenReturn(listOf(pm))

        mockMvc.perform(get("/api/v1/policies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].policyNo").value("POL-001"))
    }

    @Test @DisplayName("GET /api/v1/policies → 200 OK with empty list when no policies")
    fun getAllPolicies_empty() {
        whenever(service.getAllPolicies()).thenReturn(emptyList())
        mockMvc.perform(get("/api/v1/policies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
    }

    // ── GET /api/v1/policies/{policyNo}/attributes ───────────

    @Test @DisplayName("GET /{policyNo}/attributes → 200 OK with attribute values")
    fun getAttributesForPolicy_success() {
        whenever(service.getAttributesForPolicy("POL-001")).thenReturn(listOf(attrDto))
        mockMvc.perform(get("/api/v1/policies/POL-001/attributes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].attributeCode").value("STR_ATTR"))
            .andExpect(jsonPath("$[0].attributeValue").value("hello"))
    }

    @Test @DisplayName("GET /{policyNo}/attributes → 200 OK with empty list")
    fun getAttributesForPolicy_empty() {
        whenever(service.getAttributesForPolicy("POL-EMPTY")).thenReturn(emptyList())
        mockMvc.perform(get("/api/v1/policies/POL-EMPTY/attributes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
    }

    // ── PUT /api/v1/policies/{policyNo}/attributes/{code} ────

    @Test @DisplayName("PUT /{policyNo}/attributes/{code} → 200 OK on success")
    fun updateValue_success() {
        whenever(service.updateAttributeValue("POL-001", "STR_ATTR", "world")).thenReturn(attrDto)
        mockMvc.perform(put("/api/v1/policies/POL-001/attributes/STR_ATTR")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mapOf("attributeValue" to "world"))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.policyNo").value("POL-001"))
    }

    @Test @DisplayName("PUT /{policyNo}/attributes/{code} → 404 when attribute not found")
    fun updateValue_attributeNotFound() {
        whenever(service.updateAttributeValue(eq("POL-001"), eq("GHOST"), any()))
            .thenThrow(ResourceNotFoundException("AttributeMaster", "GHOST"))
        mockMvc.perform(put("/api/v1/policies/POL-001/attributes/GHOST")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mapOf("attributeValue" to "val"))))
            .andExpect(status().isNotFound)
    }

    @Test @DisplayName("PUT /{policyNo}/attributes/{code} → 400 when validation fails")
    fun updateValue_validationFail() {
        whenever(service.updateAttributeValue(eq("POL-001"), eq("NUM_ATTR"), any()))
            .thenThrow(AttributeValidationException("NUM_ATTR", "Expected a valid number"))
        mockMvc.perform(put("/api/v1/policies/POL-001/attributes/NUM_ATTR")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mapOf("attributeValue" to "abc"))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    // ── POST /api/v1/policies/{policyNo}/attributes ──────────

    @Test @DisplayName("POST /{policyNo}/attributes → 200 OK on bulk save")
    fun bulkSave_success() {
        whenever(service.bulkSaveForPolicy(eq("POL-001"), any())).thenReturn(listOf(attrDto))
        mockMvc.perform(post("/api/v1/policies/POL-001/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(listOf(attrDto))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].attributeCode").value("STR_ATTR"))
    }

    @Test @DisplayName("POST /{policyNo}/attributes → 400 when ARCHIVED attribute used")
    fun bulkSave_archivedAttribute() {
        whenever(service.bulkSaveForPolicy(eq("POL-001"), any()))
            .thenThrow(AttributeValidationException("ARC_ATTR", "Cannot assign values to ARCHIVED attribute"))
        mockMvc.perform(post("/api/v1/policies/POL-001/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(listOf(attrDto))))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("ARCHIVED")))
    }
}
