package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.BulkUploadResultDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.service.BulkUploadService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.nio.charset.StandardCharsets

@WebMvcTest(BulkUploadController::class)
@DisplayName("BulkUploadController Web Layer Tests")
class BulkUploadControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var service: BulkUploadService

    private fun csvFile(content: String) = MockMultipartFile(
        "file", "upload.csv", "text/plain", content.toByteArray(StandardCharsets.UTF_8)
    )

    @Test @DisplayName("POST /api/v1/bulk-upload → 200 OK with result summary on success")
    fun upload_success() {
        val resultDto = BulkUploadResultDto(totalRows = 3, successCount = 3, errorCount = 0, errors = emptyList())
        whenever(service.processCsv(any())).thenReturn(resultDto)

        mockMvc.perform(multipart("/api/v1/bulk-upload")
            .file(csvFile("policy_no,attribute_code,attribute_value\nP1,A1,v1\n")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalRows").value(3))
            .andExpect(jsonPath("$.successCount").value(3))
            .andExpect(jsonPath("$.errorCount").value(0))
    }

    @Test @DisplayName("POST /api/v1/bulk-upload → 200 OK with partial errors in result body")
    fun upload_partialErrors() {
        val rowError = BulkUploadResultDto.RowError(rowNumber = 2, policyNo = "P1", attributeCode = "BAD", errorMessage = "not found")
        val resultDto = BulkUploadResultDto(totalRows = 2, successCount = 1, errorCount = 1, errors = listOf(rowError))
        whenever(service.processCsv(any())).thenReturn(resultDto)

        mockMvc.perform(multipart("/api/v1/bulk-upload")
            .file(csvFile("policy_no,attribute_code,attribute_value\nP1,A1,v1\nP2,BAD,v2\n")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.errorCount").value(1))
            .andExpect(jsonPath("$.errors[0].attributeCode").value("BAD"))
    }

    @Test @DisplayName("POST /api/v1/bulk-upload → 400 when service throws AttributeValidationException (empty CSV)")
    fun upload_emptyCsvThrows400() {
        whenever(service.processCsv(any())).thenThrow(AttributeValidationException("CSV file is empty"))

        mockMvc.perform(multipart("/api/v1/bulk-upload").file(csvFile("")))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }
}
