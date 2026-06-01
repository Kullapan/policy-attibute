package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.BulkUploadResultDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.service.BulkUploadService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@WebFluxTest(BulkUploadController::class)
@DisplayName("BulkUploadController Web Layer Tests")
class BulkUploadControllerTest {

    @Autowired lateinit var webTestClient: WebTestClient
    @MockkBean lateinit var service: BulkUploadService

    private fun multipartData(content: String, filename: String = "upload.csv", contentType: MediaType = MediaType.TEXT_PLAIN) = 
        MultipartBodyBuilder().apply {
            part("file", content.toByteArray(), contentType)
                .filename(filename)
        }.build()

    @Test @DisplayName("POST /api/v1/bulk-upload → 200 OK with result summary on success")
    fun upload_success() {
        val resultDto = BulkUploadResultDto(totalRows = 3, successCount = 3, errorCount = 0, errors = emptyList())
        coEvery { service.processCsv(any()) } returns resultDto

        webTestClient.post().uri("/api/v1/bulk-upload")
            .body(BodyInserters.fromMultipartData(multipartData("policy_no,attribute_code,attribute_value\nP1,A1,v1\n")))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.totalRows").isEqualTo(3)
            .jsonPath("$.successCount").isEqualTo(3)
            .jsonPath("$.errorCount").isEqualTo(0)
    }

    @Test @DisplayName("POST /api/v1/bulk-upload → 200 OK with partial errors in result body")
    fun upload_partialErrors() {
        val rowError = BulkUploadResultDto.RowError(rowNumber = 2, policyNo = "P1", attributeCode = "BAD", errorMessage = "not found")
        val resultDto = BulkUploadResultDto(totalRows = 2, successCount = 1, errorCount = 1, errors = listOf(rowError))
        coEvery { service.processCsv(any()) } returns resultDto

        webTestClient.post().uri("/api/v1/bulk-upload")
            .body(BodyInserters.fromMultipartData(multipartData("policy_no,attribute_code,attribute_value\nP1,A1,v1\nP2,BAD,v2\n")))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.errorCount").isEqualTo(1)
            .jsonPath("$.errors[0].attributeCode").isEqualTo("BAD")
    }

    @Test @DisplayName("POST /api/v1/bulk-upload → 400 when service throws AttributeValidationException (empty CSV)")
    fun upload_emptyCsvThrows400() {
        coEvery { service.processCsv(any()) } throws AttributeValidationException("CSV file is empty")

        webTestClient.post().uri("/api/v1/bulk-upload")
            .body(BodyInserters.fromMultipartData(multipartData("")))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
    }
}
