package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.service.AttributeMasterService
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AttributeMasterController::class)
@DisplayName("AttributeMasterController Web Layer Tests")
class AttributeMasterControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @MockBean lateinit var service: AttributeMasterService

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
        whenever(service.listAttributes(null, null)).thenReturn(listOf(sampleDto))
        mockMvc.perform(get("/api/v1/attributes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].code").value("MAX_LIMIT"))
    }

    @Test @DisplayName("GET /api/v1/attributes?search=limit&status=ACTIVE → delegates filter params")
    fun list_withFilters() {
        whenever(service.listAttributes("limit", AttributeStatus.ACTIVE)).thenReturn(listOf(sampleDto))
        mockMvc.perform(get("/api/v1/attributes").param("search", "limit").param("status", "ACTIVE"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].code").value("MAX_LIMIT"))
    }

    // ── GET /api/v1/attributes/{code} ────────────────────────

    @Test @DisplayName("GET /api/v1/attributes/{code} → 200 OK for existing code")
    fun getByCode_found() {
        whenever(service.getByCode("MAX_LIMIT")).thenReturn(sampleDto)
        mockMvc.perform(get("/api/v1/attributes/MAX_LIMIT"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("MAX_LIMIT"))
            .andExpect(jsonPath("$.dataType").value("NUMBER"))
    }

    @Test @DisplayName("GET /api/v1/attributes/{code} → 404 for unknown code")
    fun getByCode_notFound() {
        whenever(service.getByCode("GHOST")).thenThrow(ResourceNotFoundException("AttributeMaster", "GHOST"))
        mockMvc.perform(get("/api/v1/attributes/GHOST"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    // ── POST /api/v1/attributes ──────────────────────────────

    @Test @DisplayName("POST /api/v1/attributes → 201 Created on valid body")
    fun create_valid() {
        whenever(service.create(any())).thenReturn(sampleDto)
        mockMvc.perform(post("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.code").value("MAX_LIMIT"))
    }

    @Test @DisplayName("POST /api/v1/attributes → 400 Bad Request when code is blank")
    fun create_blankCode_returns400() {
        val invalid = AttributeMasterDto(code = "", displayName = "X", dataType = DataType.STRING)
        mockMvc.perform(post("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest)
    }

    @Test @DisplayName("POST /api/v1/attributes → 400 when display name is missing")
    fun create_missingDisplayName_returns400() {
        val invalid = AttributeMasterDto(code = "VALID_CODE", displayName = "", dataType = DataType.STRING)
        mockMvc.perform(post("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest)
    }

    @Test @DisplayName("POST /api/v1/attributes → 400 when service throws AttributeValidationException")
    fun create_serviceThrows_returns400() {
        whenever(service.create(any())).thenThrow(AttributeValidationException("MAX_LIMIT", "already exists"))
        mockMvc.perform(post("/api/v1/attributes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value(containsString("already exists")))
    }

    // ── PUT /api/v1/attributes/{code} ────────────────────────

    @Test @DisplayName("PUT /api/v1/attributes/{code} → 200 OK on valid update")
    fun update_valid() {
        whenever(service.update(eq("MAX_LIMIT"), any())).thenReturn(sampleDto)
        mockMvc.perform(put("/api/v1/attributes/MAX_LIMIT")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("MAX_LIMIT"))
    }

    @Test @DisplayName("PUT /api/v1/attributes/{code} → 404 when attribute not found")
    fun update_notFound() {
        whenever(service.update(eq("GHOST"), any())).thenThrow(ResourceNotFoundException("AttributeMaster", "GHOST"))
        mockMvc.perform(put("/api/v1/attributes/GHOST")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isNotFound)
    }

    // ── DELETE /api/v1/attributes/{code} ────────────────────

    @Test @DisplayName("DELETE /api/v1/attributes/{code} → 204 No Content on success")
    fun delete_success() {
        doNothing().whenever(service).softDelete("MAX_LIMIT")
        mockMvc.perform(delete("/api/v1/attributes/MAX_LIMIT"))
            .andExpect(status().isNoContent)
    }

    @Test @DisplayName("DELETE /api/v1/attributes/{code} → 404 when attribute not found")
    fun delete_notFound() {
        doThrow(ResourceNotFoundException("AttributeMaster", "GHOST")).whenever(service).softDelete("GHOST")
        mockMvc.perform(delete("/api/v1/attributes/GHOST"))
            .andExpect(status().isNotFound)
    }
}
