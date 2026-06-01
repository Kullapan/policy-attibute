package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.BulkUploadResultDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.model.PolicyAttributeValue
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.PolicyAttributeValueRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Instant

@ExtendWith(MockKExtension::class)
@DisplayName("BulkUploadService Unit Tests")
class BulkUploadServiceTest {

    @MockK lateinit var valueRepository: PolicyAttributeValueRepository
    @MockK lateinit var masterRepository: AttributeMasterRepository
    @MockK lateinit var policyAttributeService: PolicyAttributeService
    @InjectMockKs lateinit var service: BulkUploadService

    companion object {
        private const val HEADER = "policy_no,attribute_code,attribute_value\n"
    }

    private fun buildMaster(code: String, type: DataType) = AttributeMaster(
        code = code, displayName = code, dataType = type,
        status = AttributeStatus.ACTIVE, isRequired = false,
        version = 1L, createdAt = Instant.now(), updatedAt = Instant.now(), createdBy = "system"
    )

    private fun mockFilePart(content: String): FilePart {
        val filePart = mockk<FilePart>()
        every { filePart.filename() } returns "test.csv"
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv")
        every { filePart.headers() } returns headers
        
        coEvery { filePart.transferTo(any<java.nio.file.Path>()) } coAnswers {
            val path = firstArg<java.nio.file.Path>()
            Files.write(path, content.toByteArray(StandardCharsets.UTF_8))
            Mono.empty<Void>()
        }
        return filePart
    }

    // ── Empty / Malformed CSV ────────────────────────────────

    @Test @DisplayName("Empty CSV file (no header) → throws AttributeValidationException")
    fun emptyCsv_throws() {
        val filePart = mockFilePart("")
        assertThatThrownBy { runBlocking { service.processCsv(filePart) } }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("empty")
    }

    @Test @DisplayName("Header only → totalRows=0, successCount=0, no errors")
    fun headerOnlyCsv_returnsZeroCounts() = runBlocking {
        val filePart = mockFilePart(HEADER)
        val result = service.processCsv(filePart)
        assertThat(result.totalRows).isZero()
        assertThat(result.successCount).isZero()
        assertThat(result.errors).isEmpty()
    }

    @Test @DisplayName("Row with fewer than 3 columns → records row error, continues")
    fun rowTooFewColumns_recordsError() = runBlocking {
        val filePart = mockFilePart(HEADER + "POL-001,STR_ATTR\n")
        val result = service.processCsv(filePart)
        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].errorMessage).contains("fewer than 3 columns")
        assertThat(result.successCount).isZero()
    }

    // ── Attribute Not Found ──────────────────────────────────

    @Test @DisplayName("Unknown attribute code → records ResourceNotFoundException error, continues")
    fun unknownAttributeCode_recordsError() = runBlocking {
        val csv = HEADER + "POL-001,MISSING_CODE,val\n"
        val filePart = mockFilePart(csv)
        coEvery { masterRepository.findById("MISSING_CODE") } returns null

        val result = service.processCsv(filePart)

        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].attributeCode).isEqualTo("MISSING_CODE")
        verify(exactly = 0) { valueRepository.saveAll(any<Iterable<PolicyAttributeValue>>()) }
    }

    // ── Validation Failures ──────────────────────────────────

    @Test @DisplayName("Data-type validation failure → records error and continues")
    fun dataTypeValidationFail_recordsError() = runBlocking {
        val csv = HEADER + "POL-001,NUM_ATTR,abc\n"
        val filePart = mockFilePart(csv)
        val master = buildMaster("NUM_ATTR", DataType.NUMBER)
        coEvery { masterRepository.findById("NUM_ATTR") } returns master
        every { policyAttributeService.validateDataType(master, "abc") } throws 
            AttributeValidationException("NUM_ATTR", "Expected a valid number but got: abc")

        val result = service.processCsv(filePart)

        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].errorMessage).contains("Expected a valid number")
        verify(exactly = 0) { valueRepository.saveAll(any<Iterable<PolicyAttributeValue>>()) }
    }

    @Test @DisplayName("Regex validation failure → records error and continues")
    fun regexValidationFail_recordsError() = runBlocking {
        val csv = HEADER + "POL-001,STR_ATTR,WRONG\n"
        val filePart = mockFilePart(csv)
        val master = buildMaster("STR_ATTR", DataType.STRING)
        coEvery { masterRepository.findById("STR_ATTR") } returns master
        every { policyAttributeService.validateDataType(master, "WRONG") } returns Unit
        every { policyAttributeService.validateValueAgainstRegex(master, "WRONG") } throws 
            AttributeValidationException("STR_ATTR", "Does not match regex")

        val result = service.processCsv(filePart)

        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.errors[0].errorMessage).contains("regex")
    }

    // ── Mixed Rows ───────────────────────────────────────────

    @Test @DisplayName("Mix of valid and invalid rows → saves valid only, reports all errors")
    fun mixedRows_savesValidAndReportsErrors() = runBlocking {
        val csv = HEADER +
                "POL-001,STR_ATTR,valid_value\n" +
                "POL-002,NUM_ATTR,bad\n" +
                "POL-003,MISSING\n"
        val filePart = mockFilePart(csv)
        val strMaster = buildMaster("STR_ATTR", DataType.STRING)
        val numMaster = buildMaster("NUM_ATTR", DataType.NUMBER)

        coEvery { masterRepository.findById("STR_ATTR") } returns strMaster
        every { policyAttributeService.validateDataType(strMaster, "valid_value") } returns Unit
        every { policyAttributeService.validateValueAgainstRegex(strMaster, "valid_value") } returns Unit

        coEvery { masterRepository.findById("NUM_ATTR") } returns numMaster
        every { policyAttributeService.validateDataType(numMaster, "bad") } throws 
            AttributeValidationException("NUM_ATTR", "Expected a valid number but got: bad")

        val slot = slot<Iterable<PolicyAttributeValue>>()
        every { valueRepository.saveAll(capture(slot)) } answers { (firstArg<Iterable<PolicyAttributeValue>>()).asFlow() }

        val result = service.processCsv(filePart)

        assertThat(result.totalRows).isEqualTo(3)
        assertThat(result.successCount).isEqualTo(1)
        assertThat(result.errorCount).isEqualTo(2)

        assertThat(slot.captured.toList()).hasSize(1)
    }

    // ── All Valid ────────────────────────────────────────────

    @Test @DisplayName("All valid rows → saves all via saveAll, zero errors")
    fun allValid_savesAll() = runBlocking {
        val csv = HEADER + "POL-001,STR_ATTR,hello\n" + "POL-002,STR_ATTR,world\n"
        val filePart = mockFilePart(csv)
        val master = buildMaster("STR_ATTR", DataType.STRING)
        coEvery { masterRepository.findById("STR_ATTR") } returns master
        every { policyAttributeService.validateDataType(any(), any()) } returns Unit
        every { policyAttributeService.validateValueAgainstRegex(any(), any()) } returns Unit

        val slot = slot<Iterable<PolicyAttributeValue>>()
        every { valueRepository.saveAll(capture(slot)) } answers { (firstArg<Iterable<PolicyAttributeValue>>()).asFlow() }

        val result = service.processCsv(filePart)

        assertThat(result.totalRows).isEqualTo(2)
        assertThat(result.successCount).isEqualTo(2)
        assertThat(result.errorCount).isZero()

        assertThat(slot.captured.toList()).hasSize(2)
    }

    @Test @DisplayName("All invalid rows → never calls saveAll, all errors reported")
    fun allInvalid_neverSaves() = runBlocking {
        val csv = HEADER + "POL-001,BAD\n" + "POL-002,ALSO_BAD\n"
        val filePart = mockFilePart(csv)
        val result = service.processCsv(filePart)
        assertThat(result.successCount).isZero()
        assertThat(result.errorCount).isEqualTo(2)
        verify(exactly = 0) { valueRepository.saveAll(any<Iterable<PolicyAttributeValue>>()) }
    }

    // ── Row number accounting ─────────────────────────────────

    @Test @DisplayName("Row numbers in errors reflect header offset (+1)")
    fun rowNumbers_reflectHeaderOffset() = runBlocking {
        val csv = HEADER + "P1,ONLY_TWO\n" + "P2,ALSO_TWO\n"
        val filePart = mockFilePart(csv)
        val result = service.processCsv(filePart)
        assertThat(result.errors[0].rowNumber).isEqualTo(2)
        assertThat(result.errors[1].rowNumber).isEqualTo(3)
    }
}
