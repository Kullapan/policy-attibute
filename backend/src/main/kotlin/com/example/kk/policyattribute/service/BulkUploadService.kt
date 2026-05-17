package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.BulkUploadResultDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.PolicyAttributeValue
import com.example.kk.policyattribute.model.PolicyAttributeValueId
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.PolicyAttributeValueRepository
import com.opencsv.CSVReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Service for bulk CSV upload of policy attribute values.
 * Uses streaming (line-by-line) to minimize memory footprint.
 *
 * Expected CSV format:
 *   policy_no, attribute_code, attribute_value
 */
@Service
class BulkUploadService(
    private val valueRepository: PolicyAttributeValueRepository,
    private val masterRepository: AttributeMasterRepository,
    private val policyAttributeService: PolicyAttributeService
) {
    private val log = LoggerFactory.getLogger(BulkUploadService::class.java)

    @Transactional
    fun processCsv(file: MultipartFile): BulkUploadResultDto {
        val errors = mutableListOf<BulkUploadResultDto.RowError>()
        val validEntities = mutableListOf<PolicyAttributeValue>()
        var totalRows = 0

        try {
            CSVReader(InputStreamReader(file.inputStream, StandardCharsets.UTF_8)).use { reader ->
                // Skip header row
                reader.readNext() ?: throw AttributeValidationException("CSV file is empty")

                var row: Array<String>?
                while (reader.readNext().also { row = it } != null) {
                    totalRows++
                    val rowNum = totalRows + 1 // +1 for header offset
                    val r = row!!

                    if (r.size < 3) {
                        errors += BulkUploadResultDto.RowError(
                            rowNumber    = rowNum,
                            errorMessage = "Row has fewer than 3 columns"
                        )
                        continue
                    }

                    val policyNo       = sanitizeCsvCell(r[0].trim())
                    val attributeCode  = sanitizeCsvCell(r[1].trim())
                    val attributeValue = sanitizeCsvCell(r[2].trim())

                    try {
                        val master = masterRepository.findById(attributeCode)
                            .orElseThrow { ResourceNotFoundException("AttributeMaster", attributeCode) }

                        policyAttributeService.validateDataType(master, attributeValue)
                        policyAttributeService.validateValueAgainstRegex(master, attributeValue)

                        val id = PolicyAttributeValueId(policyNo, attributeCode)
                        validEntities += PolicyAttributeValue(id = id, attributeValue = attributeValue)

                    } catch (e: Exception) {
                        when (e) {
                            is ResourceNotFoundException, is AttributeValidationException ->
                                errors += BulkUploadResultDto.RowError(
                                    rowNumber     = rowNum,
                                    policyNo      = policyNo,
                                    attributeCode = attributeCode,
                                    errorMessage  = e.message
                                )
                            else -> throw e
                        }
                    }
                }
            }
        } catch (e: AttributeValidationException) {
            throw e
        } catch (e: Exception) {
            throw AttributeValidationException("Failed to parse CSV file: ${e.message}")
        }

        if (validEntities.isNotEmpty()) {
            valueRepository.saveAll(validEntities)
            log.info("Bulk upload: saved {} records", validEntities.size)
        }

        return BulkUploadResultDto(
            totalRows    = totalRows,
            successCount = validEntities.size,
            errorCount   = errors.size,
            errors       = errors
        )
    }

    // ── Private helpers ──────────────────────────────────────

    /**
     * Sanitizes a CSV cell value to prevent formula injection attacks.
     * (OWASP mitigation: prepend single quote to neutralize formula triggers)
     *
     * Affected trigger characters: '=', '+', '-', '@', '\r', '\t'
     */
    private fun sanitizeCsvCell(value: String): String {
        val formulaTriggers = charArrayOf('=', '+', '-', '@', '\r', '\t')
        return if (value.isNotEmpty() && value.first() in formulaTriggers) "'$value" else value
    }
}
