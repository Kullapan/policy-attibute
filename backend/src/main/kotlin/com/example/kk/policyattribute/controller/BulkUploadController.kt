package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.BulkUploadResultDto
import com.example.kk.policyattribute.service.BulkUploadService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for CSV bulk upload of policy attribute values.
 */
@RestController
@RequestMapping("/api/v1/bulk-upload")
class BulkUploadController(
    private val service: BulkUploadService
) {

    /**
     * Upload a CSV file with policy attribute values.
     * Expected CSV columns: policy_no, attribute_code, attribute_value
     *
     * Validates file extension (.csv) and MIME type before processing to
     * prevent file-type bypass and parser exploitation attacks.
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<BulkUploadResultDto> {
        // ── File extension validation ──
        val filename = file.originalFilename ?: ""
        if (!filename.endsWith(".csv", ignoreCase = true)) {
            throw IllegalArgumentException("Only CSV files are accepted (.csv extension required)")
        }

        // ── MIME type validation ──
        val allowedTypes = setOf("text/csv", "application/vnd.ms-excel", "application/octet-stream", "text/plain")
        val contentType = file.contentType
        if (contentType != null && contentType !in allowedTypes) {
            throw IllegalArgumentException("Invalid file content type: '$contentType'. Only CSV files are accepted.")
        }

        return ResponseEntity.ok(service.processCsv(file))
    }
}

