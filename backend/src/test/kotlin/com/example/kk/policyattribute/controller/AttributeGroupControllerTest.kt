package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.AttributeGroupDto
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.service.AttributeGroupService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.asFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(AttributeGroupController::class)
@DisplayName("AttributeGroupController Web Layer Tests")
class AttributeGroupControllerTest {

    @Autowired lateinit var webTestClient: WebTestClient
    @MockkBean lateinit var service: AttributeGroupService

    lateinit var sampleDto: AttributeGroupDto

    @BeforeEach
    fun setUp() {
        sampleDto = AttributeGroupDto(
            code = "CONSENT",
            displayNameEn = "Consent",
            displayNameTh = "ความยินยอม (Consent)",
            displayOrder = 1,
            status = AttributeStatus.ACTIVE
        )
    }

    @Test @DisplayName("GET /api/v1/attribute-groups → 200 OK with list")
    fun list_returnsOk() {
        every { service.listGroups(false) } returns listOf(sampleDto).asFlow()
        webTestClient.get().uri("/api/v1/attribute-groups")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].code").isEqualTo("CONSENT")
            .jsonPath("$[0].displayNameEn").isEqualTo("Consent")
            .jsonPath("$[0].displayNameTh").isEqualTo("ความยินยอม (Consent)")
    }

    @Test @DisplayName("GET /api/v1/attribute-groups?includeArchived=true → 200 OK with all list")
    fun listWithArchived_returnsOk() {
        every { service.listGroups(true) } returns listOf(sampleDto).asFlow()
        webTestClient.get().uri("/api/v1/attribute-groups?includeArchived=true")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].code").isEqualTo("CONSENT")
    }

    @Test @DisplayName("GET /api/v1/attribute-groups/{code} → 200 OK")
    fun getByCode_returnsOk() {
        coEvery { service.getByCode("CONSENT") } returns sampleDto
        webTestClient.get().uri("/api/v1/attribute-groups/CONSENT")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("CONSENT")
    }

    @Test @DisplayName("POST /api/v1/attribute-groups → 201 Created")
    fun create_returnsCreated() {
        coEvery { service.create(any()) } returns sampleDto
        webTestClient.post().uri("/api/v1/attribute-groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(sampleDto)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.code").isEqualTo("CONSENT")
    }

    @Test @DisplayName("PUT /api/v1/attribute-groups/{code} → 200 OK")
    fun update_returnsOk() {
        coEvery { service.update("CONSENT", any()) } returns sampleDto
        webTestClient.put().uri("/api/v1/attribute-groups/CONSENT")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(sampleDto)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("CONSENT")
    }

    @Test @DisplayName("DELETE /api/v1/attribute-groups/{code} → 204 No Content")
    fun delete_returnsNoContent() {
        coEvery { service.softDelete("CONSENT") } returns Unit
        webTestClient.delete().uri("/api/v1/attribute-groups/CONSENT")
            .exchange()
            .expectStatus().isNoContent
    }
}
