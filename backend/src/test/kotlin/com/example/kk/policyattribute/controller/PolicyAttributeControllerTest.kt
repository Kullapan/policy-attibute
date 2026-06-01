package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.CreatePolicyRequestDto
import com.example.kk.policyattribute.dto.PolicyAttributeValueDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.PolicyMaster
import com.example.kk.policyattribute.service.PolicyAttributeService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

@WebFluxTest(PolicyAttributeController::class)
@DisplayName("PolicyAttributeController Web Layer Tests")
class PolicyAttributeControllerTest {

    @Autowired lateinit var webTestClient: WebTestClient
    @MockkBean lateinit var service: PolicyAttributeService

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
        coEvery { service.createPolicyWithAttributes(any()) } returns listOf(attrDto)

        webTestClient.post().uri("/api/v1/policies/create")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].policyNo").isEqualTo("POL-001")
    }

    @Test @DisplayName("POST /create → 400 when policy already exists (IllegalArgumentException)")
    fun createPolicy_alreadyExists() {
        val req = CreatePolicyRequestDto(policyNo = "POL-DUP")
        coEvery { service.createPolicyWithAttributes(any()) } throws IllegalArgumentException("Policy already exists: POL-DUP")

        webTestClient.post().uri("/api/v1/policies/create")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isBadRequest
    }

    // ── GET /api/v1/policies ─────────────────────────────────

    @Test @DisplayName("GET /api/v1/policies → 200 OK with policy list")
    fun getAllPolicies_success() {
        val pm = PolicyMaster(policyNo = "POL-001", status = "ACTIVE")
        every { service.getAllPolicies() } returns listOf(pm).asFlow()

        webTestClient.get().uri("/api/v1/policies")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].policyNo").isEqualTo("POL-001")
    }

    @Test @DisplayName("GET /api/v1/policies → 200 OK with empty list when no policies")
    fun getAllPolicies_empty() {
        every { service.getAllPolicies() } returns emptyFlow()
        webTestClient.get().uri("/api/v1/policies")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isEmpty
    }

    // ── GET /api/v1/policies/{policyNo}/attributes ───────────

    @Test @DisplayName("GET /{policyNo}/attributes → 200 OK with attribute values")
    fun getAttributesForPolicy_success() {
        coEvery { service.getAttributesForPolicy("POL-001") } returns listOf(attrDto)
        webTestClient.get().uri("/api/v1/policies/POL-001/attributes")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].attributeCode").isEqualTo("STR_ATTR")
            .jsonPath("$[0].attributeValue").isEqualTo("hello")
    }

    @Test @DisplayName("GET /{policyNo}/attributes → 200 OK with empty list")
    fun getAttributesForPolicy_empty() {
        coEvery { service.getAttributesForPolicy("POL-EMPTY") } returns emptyList()
        webTestClient.get().uri("/api/v1/policies/POL-EMPTY/attributes")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isEmpty
    }

    // ── PUT /api/v1/policies/{policyNo}/attributes/{code} ────

    @Test @DisplayName("PUT /{policyNo}/attributes/{code} → 200 OK on success")
    fun updateValue_success() {
        coEvery { service.updateAttributeValue("POL-001", "STR_ATTR", "world") } returns attrDto
        webTestClient.put().uri("/api/v1/policies/POL-001/attributes/STR_ATTR")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("attributeValue" to "world"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.policyNo").isEqualTo("POL-001")
    }

    @Test @DisplayName("PUT /{policyNo}/attributes/{code} → 404 when attribute not found")
    fun updateValue_attributeNotFound() {
        coEvery { service.updateAttributeValue("POL-001", "GHOST", any()) } throws ResourceNotFoundException("AttributeMaster", "GHOST")
        webTestClient.put().uri("/api/v1/policies/POL-001/attributes/GHOST")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("attributeValue" to "val"))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test @DisplayName("PUT /{policyNo}/attributes/{code} → 400 when validation fails")
    fun updateValue_validationFail() {
        coEvery { service.updateAttributeValue("POL-001", "NUM_ATTR", any()) } throws AttributeValidationException("NUM_ATTR", "Expected a valid number")
        webTestClient.put().uri("/api/v1/policies/POL-001/attributes/NUM_ATTR")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("attributeValue" to "abc"))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }

    // ── POST /api/v1/policies/{policyNo}/attributes ──────────

    @Test @DisplayName("POST /{policyNo}/attributes → 200 OK on bulk save")
    fun bulkSave_success() {
        coEvery { service.bulkSaveForPolicy("POL-001", any()) } returns listOf(attrDto)
        webTestClient.post().uri("/api/v1/policies/POL-001/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(listOf(attrDto))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].attributeCode").isEqualTo("STR_ATTR")
    }

    @Test @DisplayName("POST /{policyNo}/attributes → 400 when ARCHIVED attribute used")
    fun bulkSave_archivedAttribute() {
        coEvery { service.bulkSaveForPolicy("POL-001", any()) } throws AttributeValidationException("ARC_ATTR", "Cannot assign values to ARCHIVED attribute")
        webTestClient.post().uri("/api/v1/policies/POL-001/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(listOf(attrDto))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").value(containsString("ARCHIVED"))
    }
}
