package com.example.kk.policyattribute.dto

/**
 * Response DTO for bulk upload operations.
 * Contains success/error counts and detailed error list.
 */
data class BulkUploadResultDto(
    val totalRows: Int = 0,
    val successCount: Int = 0,
    val errorCount: Int = 0,
    val errors: List<RowError> = emptyList()
) {
    data class RowError(
        val rowNumber: Int = 0,
        val policyNo: String? = null,
        val attributeCode: String? = null,
        val errorMessage: String? = null
    )
}
