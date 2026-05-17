package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.BulkUploadResultDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.model.PolicyAttributeValue
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.PolicyAttributeValueRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.mock.web.MockMultipartFile
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("BulkUploadService Unit Tests")
class BulkUploadServiceTest {

    @Mock lateinit var valueRepository: PolicyAttributeValueRepository
    @Mock lateinit var masterRepository: AttributeMasterRepository
    @Mock lateinit var policyAttributeService: PolicyAttributeService
    @InjectMocks lateinit var service: BulkUploadService

    companion object {
        private const val HEADER = "policy_no,attribute_code,attribute_value\n"
    }

    private fun buildMaster(code: String, type: DataType) = AttributeMaster(
        code = code, displayName = code, dataType = type,
        status = AttributeStatus.ACTIVE, isRequired = false,
        version = 1L, createdAt = Instant.now(), updatedAt = Instant.now(), createdBy = "system"
    )

    private fun csvFile(content: String) = MockMultipartFile(
        "file", "test.csv", "text/csv", content.toByteArray(StandardCharsets.UTF_8)
    )

    // ── Empty / Malformed CSV ────────────────────────────────

    @Test @DisplayName("Empty CSV file (no header) → throws AttributeValidationException")
    fun emptyCsv_throws() {
        assertThatThrownBy { service.processCsv(csvFile("")) }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("empty")
    }

    @Test @DisplayName("Header only → totalRows=0, successCount=0, no errors")
    fun headerOnlyCsv_returnsZeroCounts() {
        val result = service.processCsv(csvFile(HEADER))
        assertThat(result.totalRows).isZero()
        assertThat(result.successCount).isZero()
        assertThat(result.errors).isEmpty()
    }

    @Test @DisplayName("Row with fewer than 3 columns → records row error, continues")
    fun rowTooFewColumns_recordsError() {
        val result = service.processCsv(csvFile(HEADER + "POL-001,STR_ATTR\n"))
        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].errorMessage).contains("fewer than 3 columns")
        assertThat(result.successCount).isZero()
    }

    // ── Attribute Not Found ──────────────────────────────────

    @Test @DisplayName("Unknown attribute code → records ResourceNotFoundException error, continues")
    fun unknownAttributeCode_recordsError() {
        val csv = HEADER + "POL-001,MISSING_CODE,val\n"
        whenever(masterRepository.findById("MISSING_CODE")).thenReturn(Optional.empty())

        val result = service.processCsv(csvFile(csv))

        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].attributeCode).isEqualTo("MISSING_CODE")
        @Suppress("UNCHECKED_CAST")
        verify(valueRepository, never()).saveAll(any<Iterable<PolicyAttributeValue>>())
    }

    // ── Validation Failures ──────────────────────────────────

    @Test @DisplayName("Data-type validation failure → records error and continues")
    fun dataTypeValidationFail_recordsError() {
        val master = buildMaster("NUM_ATTR", DataType.NUMBER)
        whenever(masterRepository.findById("NUM_ATTR")).thenReturn(Optional.of(master))
        doThrow(AttributeValidationException("NUM_ATTR", "Expected a valid number but got: abc"))
            .whenever(policyAttributeService).validateDataType(master, "abc")

        val result = service.processCsv(csvFile(HEADER + "POL-001,NUM_ATTR,abc\n"))

        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].errorMessage).contains("Expected a valid number")
        @Suppress("UNCHECKED_CAST")
        verify(valueRepository, never()).saveAll(any<Iterable<PolicyAttributeValue>>())
    }

    @Test @DisplayName("Regex validation failure → records error and continues")
    fun regexValidationFail_recordsError() {
        val master = buildMaster("STR_ATTR", DataType.STRING)
        whenever(masterRepository.findById("STR_ATTR")).thenReturn(Optional.of(master))
        doNothing().whenever(policyAttributeService).validateDataType(master, "WRONG")
        doThrow(AttributeValidationException("STR_ATTR", "Does not match regex"))
            .whenever(policyAttributeService).validateValueAgainstRegex(master, "WRONG")

        val result = service.processCsv(csvFile(HEADER + "POL-001,STR_ATTR,WRONG\n"))

        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].errorMessage).contains("regex")
    }

    // ── Mixed Rows ───────────────────────────────────────────

    @Test @DisplayName("Mix of valid and invalid rows → saves valid only, reports all errors")
    fun mixedRows_savesValidAndReportsErrors() {
        val strMaster = buildMaster("STR_ATTR", DataType.STRING)
        val numMaster = buildMaster("NUM_ATTR", DataType.NUMBER)

        whenever(masterRepository.findById("STR_ATTR")).thenReturn(Optional.of(strMaster))
        doNothing().whenever(policyAttributeService).validateDataType(strMaster, "valid_value")
        doNothing().whenever(policyAttributeService).validateValueAgainstRegex(strMaster, "valid_value")

        whenever(masterRepository.findById("NUM_ATTR")).thenReturn(Optional.of(numMaster))
        doThrow(AttributeValidationException("NUM_ATTR", "Expected a valid number but got: bad"))
            .whenever(policyAttributeService).validateDataType(numMaster, "bad")

        val csv = HEADER +
                "POL-001,STR_ATTR,valid_value\n" +
                "POL-002,NUM_ATTR,bad\n" +
                "POL-003,MISSING\n"

        val result = service.processCsv(csvFile(csv))

        assertThat(result.totalRows).isEqualTo(3)
        assertThat(result.successCount).isEqualTo(1)
        assertThat(result.errorCount).isEqualTo(2)

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(Iterable::class.java) as ArgumentCaptor<Iterable<PolicyAttributeValue>>
        verify(valueRepository).saveAll(captor.capture())
        assertThat(captor.value.toList()).hasSize(1)
    }

    // ── All Valid ────────────────────────────────────────────

    @Test @DisplayName("All valid rows → saves all via saveAll, zero errors")
    fun allValid_savesAll() {
        val master = buildMaster("STR_ATTR", DataType.STRING)
        whenever(masterRepository.findById("STR_ATTR")).thenReturn(Optional.of(master))
        doNothing().whenever(policyAttributeService).validateDataType(any(), any())
        doNothing().whenever(policyAttributeService).validateValueAgainstRegex(any(), any())

        val csv = HEADER + "POL-001,STR_ATTR,hello\n" + "POL-002,STR_ATTR,world\n"
        val result = service.processCsv(csvFile(csv))

        assertThat(result.totalRows).isEqualTo(2)
        assertThat(result.successCount).isEqualTo(2)
        assertThat(result.errorCount).isZero()

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(Iterable::class.java) as ArgumentCaptor<Iterable<PolicyAttributeValue>>
        verify(valueRepository).saveAll(captor.capture())
        assertThat(captor.value.toList()).hasSize(2)
    }

    @Test @DisplayName("All invalid rows → never calls saveAll, all errors reported")
    fun allInvalid_neverSaves() {
        val csv = HEADER + "POL-001,BAD\n" + "POL-002,ALSO_BAD\n"
        val result = service.processCsv(csvFile(csv))
        assertThat(result.successCount).isZero()
        assertThat(result.errorCount).isEqualTo(2)
        @Suppress("UNCHECKED_CAST")
        verify(valueRepository, never()).saveAll(any<Iterable<PolicyAttributeValue>>())
    }

    // ── Row number accounting ─────────────────────────────────

    @Test @DisplayName("Row numbers in errors reflect header offset (+1)")
    fun rowNumbers_reflectHeaderOffset() {
        val csv = HEADER + "P1,ONLY_TWO\n" + "P2,ALSO_TWO\n"
        val result = service.processCsv(csvFile(csv))
        assertThat(result.errors[0].rowNumber).isEqualTo(2)
        assertThat(result.errors[1].rowNumber).isEqualTo(3)
    }
}
