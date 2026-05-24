package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.AttributeGroupDto
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.service.AttributeGroupService
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AttributeGroupController::class)
@DisplayName("AttributeGroupController Web Layer Tests")
class AttributeGroupControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var service: AttributeGroupService
    @Autowired lateinit var objectMapper: ObjectMapper

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
        whenever(service.listGroups(false)).thenReturn(listOf(sampleDto))
        mockMvc.perform(get("/api/v1/attribute-groups"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].code").value("CONSENT"))
            .andExpect(jsonPath("$[0].displayNameEn").value("Consent"))
            .andExpect(jsonPath("$[0].displayNameTh").value("ความยินยอม (Consent)"))
    }

    @Test @DisplayName("GET /api/v1/attribute-groups?includeArchived=true → 200 OK with all list")
    fun listWithArchived_returnsOk() {
        whenever(service.listGroups(true)).thenReturn(listOf(sampleDto))
        mockMvc.perform(get("/api/v1/attribute-groups").param("includeArchived", "true"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].code").value("CONSENT"))
    }

    @Test @DisplayName("GET /api/v1/attribute-groups/{code} → 200 OK")
    fun getByCode_returnsOk() {
        whenever(service.getByCode("CONSENT")).thenReturn(sampleDto)
        mockMvc.perform(get("/api/v1/attribute-groups/CONSENT"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("CONSENT"))
    }

    @Test @DisplayName("POST /api/v1/attribute-groups → 201 Created")
    fun create_returnsCreated() {
        whenever(service.create(any())).thenReturn(sampleDto)
        mockMvc.perform(post("/api/v1/attribute-groups")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.code").value("CONSENT"))
    }

    @Test @DisplayName("PUT /api/v1/attribute-groups/{code} → 200 OK")
    fun update_returnsOk() {
        whenever(service.update(eq("CONSENT"), any())).thenReturn(sampleDto)
        mockMvc.perform(put("/api/v1/attribute-groups/CONSENT")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("CONSENT"))
    }

    @Test @DisplayName("DELETE /api/v1/attribute-groups/{code} → 204 No Content")
    fun delete_returnsNoContent() {
        mockMvc.perform(delete("/api/v1/attribute-groups/CONSENT"))
            .andExpect(status().isNoContent)
    }
}

