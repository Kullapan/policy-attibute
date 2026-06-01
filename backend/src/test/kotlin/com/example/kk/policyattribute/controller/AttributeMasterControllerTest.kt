package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.service.AttributeMasterService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.asFlow
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(AttributeMasterController::class)
@DisplayName("AttributeMasterController Web Layer Tests")
class AttributeMasterControllerTest {

    @Autowired lateinit var webTestClient: WebTestClient
    @MockkBean lateinit var service: AttributeMasterService

    lateinit var sampleDto: AttributeMasterDto

    @BeforeEach
    fun setUp() {
        sampleDto = AttributeMasterDto(
            code = "MAX_LIMIT",
            displayName = "Maximum Limit",
            dataType = DataType.NUMBER,
            status = AttributeStatus.ACTIVE,
            isRequired = true,
            version = 1L
        )
    }

    // ── GET /api/v1/attributes ───────────────────────────────

    @Test @DisplayName("GET /api/v1/attributes → 200 OK with list")
    fun list_returnsOk() {
        every { service.listAttributes(null, null) } returns listOf(sampleDto).asFlow()
        webTestClient.get().uri("/api/v1/attributes")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].code").isEqualTo("MAX_LIMIT")
    }

    @Test @DisplayName("GET /api/v1/attributes?search=limit&status=ACTIVE → delegates filter params")
    fun list_withFilters() {
        every { service.listAttributes("limit", AttributeStatus.ACTIVE) } returns listOf(sampleDto).asFlow()
        webTestClient.get().uri("/api/v1/attributes?search=limit&status=ACTIVE")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].code").isEqualTo("MAX_LIMIT")
    }

    // ── GET /api/v1/attributes/{code} ────────────────────────

    @Test @DisplayName("GET /api/v1/attributes/{code} → 200 OK for existing code")
    fun getByCode_found() {
        coEvery { service.getByCode("MAX_LIMIT") } returns sampleDto
        webTestClient.get().uri("/api/v1/attributes/MAX_LIMIT")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("MAX_LIMIT")
            .jsonPath("$.dataType").isEqualTo("NUMBER")
    }

    @Test @DisplayName("GET /api/v1/attributes/{code} → 404 for unknown code")
    fun getByCode_notFound() {
        coEvery { service.getByCode("GHOST") } throws ResourceNotFoundException("AttributeMaster", "GHOST")
        webTestClient.get().uri("/api/v1/attributes/GHOST")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
    }

    // ── POST /api/v1/attributes ──────────────────────────────

    @Test @DisplayName("POST /api/v1/attributes → 201 Created on valid body")
    fun create_valid() {
        coEvery { service.create(any()) } returns sampleDto
        webTestClient.post().uri("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(sampleDto)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.code").isEqualTo("MAX_LIMIT")
    }

    @Test @DisplayName("POST /api/v1/attributes → 400 Bad Request when code is blank")
    fun create_blankCode_returns400() {
        val invalid = AttributeMasterDto(code = "", displayName = "X", dataType = DataType.STRING)
        webTestClient.post().uri("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalid)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test @DisplayName("POST /api/v1/attributes → 400 when display name is missing")
    fun create_missingDisplayName_returns400() {
        val invalid = AttributeMasterDto(code = "VALID_CODE", displayName = "", dataType = DataType.STRING)
        webTestClient.post().uri("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalid)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test @DisplayName("POST /api/v1/attributes → 400 when service throws AttributeValidationException")
    fun create_serviceThrows_returns400() {
        coEvery { service.create(any()) } throws AttributeValidationException("MAX_LIMIT", "already exists")
        webTestClient.post().uri("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(sampleDto)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").value(containsString("already exists"))
    }

    // ── PUT /api/v1/attributes/{code} ────────────────────────

    @Test @DisplayName("PUT /api/v1/attributes/{code} → 200 OK on valid update")
    fun update_valid() {
        coEvery { service.update("MAX_LIMIT", any()) } returns sampleDto
        webTestClient.put().uri("/api/v1/attributes/MAX_LIMIT")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(sampleDto)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("MAX_LIMIT")
    }

    @Test @DisplayName("PUT /api/v1/attributes/{code} → 404 when attribute not found")
    fun update_notFound() {
        coEvery { service.update("GHOST", any()) } throws ResourceNotFoundException("AttributeMaster", "GHOST")
        webTestClient.put().uri("/api/v1/attributes/GHOST")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(sampleDto)
            .exchange()
            .expectStatus().isNotFound
    }

    // ── DELETE /api/v1/attributes/{code} ────────────────────

    @Test @DisplayName("DELETE /api/v1/attributes/{code} → 204 No Content on success")
    fun delete_success() {
        coEvery { service.softDelete("MAX_LIMIT") } returns Unit
        webTestClient.delete().uri("/api/v1/attributes/MAX_LIMIT")
            .exchange()
            .expectStatus().isNoContent
        coVerify(exactly = 1) { service.softDelete("MAX_LIMIT") }
    }

    @Test @DisplayName("DELETE /api/v1/attributes/{code} → 404 when attribute not found")
    fun delete_notFound() {
        coEvery { service.softDelete("GHOST") } throws ResourceNotFoundException("AttributeMaster", "GHOST")
        webTestClient.delete().uri("/api/v1/attributes/GHOST")
            .exchange()
            .expectStatus().isNotFound
    }
}
