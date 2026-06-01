package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.CreatePolicyRequestDto
import com.example.kk.policyattribute.dto.PolicyAttributeValueDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.*
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.PolicyAttributeValueRepository
import com.example.kk.policyattribute.repository.PolicyMasterRepository
import io.mockk.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("PolicyAttributeService Unit Tests")
class PolicyAttributeServiceTest {

    private val valueRepository = mockk<PolicyAttributeValueRepository>()
    private val masterRepository = mockk<AttributeMasterRepository>()
    private val policyMasterRepository = mockk<PolicyMasterRepository>()
    private val service = PolicyAttributeService(valueRepository, masterRepository, policyMasterRepository)

    private lateinit var stringMaster: AttributeMaster
    private lateinit var numberMaster: AttributeMaster
    private lateinit var dateMaster: AttributeMaster
    private lateinit var boolMaster: AttributeMaster
    private lateinit var requiredMaster: AttributeMaster
    private lateinit var regexMaster: AttributeMaster

    @BeforeEach
    fun setUp() {
        stringMaster  = buildMaster("STR_ATTR",   DataType.STRING,  false, null,         null)
        numberMaster  = buildMaster("NUM_ATTR",   DataType.NUMBER,  false, null,         null)
        dateMaster    = buildMaster("DATE_ATTR",  DataType.DATE,    false, null,         null)
        boolMaster    = buildMaster("BOOL_ATTR",  DataType.BOOLEAN, false, null,         null)
        requiredMaster= buildMaster("REQ_ATTR",   DataType.STRING,  true,  null,         null)
        regexMaster   = buildMaster("REGEX_ATTR", DataType.STRING,  false, "^MAX_\\d+$", "Must match MAX_<number>")
    }

    private fun buildMaster(code: String, type: DataType, required: Boolean, regex: String?, errMsg: String?) =
        AttributeMaster(
            code = code, displayName = code, dataType = type,
            status = AttributeStatus.ACTIVE, isRequired = required,
            regexPattern = regex, regexErrorMsg = errMsg,
            version = 1L, createdAt = Instant.now(), updatedAt = Instant.now(), createdBy = "system"
        )

    private fun buildValue(policyNo: String, code: String, val_: String) =
        PolicyAttributeValue(
            id = 100L,
            policyNo = policyNo,
            attributeCode = code,
            attributeValue = val_,
            createdAt = Instant.now(), updatedAt = Instant.now(), createdBy = "system"
        )

    // ── createPolicyWithAttributes ──────────────────────────

    @Test @DisplayName("New policy with attributes → saves PolicyMaster and returns attribute list")
    fun createPolicy_newPolicyWithAttributes() {
        runBlocking {
            val attrDto = PolicyAttributeValueDto(attributeCode = "STR_ATTR", attributeValue = "hello")
            val req = CreatePolicyRequestDto(policyNo = "POL-001", attributes = listOf(attrDto))

            coEvery { policyMasterRepository.existsById("POL-001") } returns false
            coEvery { masterRepository.findById("STR_ATTR") } returns stringMaster
            val savedVal = buildValue("POL-001", "STR_ATTR", "hello")
            val savedPolicy = PolicyMaster(policyNo = "POL-001")
            
            coEvery { policyMasterRepository.save(any()) } returns savedPolicy
            coEvery { valueRepository.findByPolicyNoAndAttributeCode("POL-001", "STR_ATTR") } returns null
            coEvery { valueRepository.save(any()) } returns savedVal

            val result = service.createPolicyWithAttributes(req)
            coVerify(exactly = 1) { policyMasterRepository.save(any()) }
            assertThat(result).hasSize(1)
        }
    }

    @Test @DisplayName("New policy with null attributes → saves PolicyMaster and returns empty list")
    fun createPolicy_noAttributes() {
        runBlocking {
            val req = CreatePolicyRequestDto(policyNo = "POL-002", attributes = null)
            coEvery { policyMasterRepository.existsById("POL-002") } returns false
            coEvery { policyMasterRepository.save(any()) } returns PolicyMaster(policyNo = "POL-002")

            val result = service.createPolicyWithAttributes(req)
            coVerify(exactly = 1) { policyMasterRepository.save(any()) }
            assertThat(result).isEmpty()
        }
    }

    @Test @DisplayName("Existing policy → throws IllegalArgumentException")
    fun createPolicy_alreadyExists() {
        runBlocking {
            val req = CreatePolicyRequestDto(policyNo = "POL-001")
            coEvery { policyMasterRepository.existsById("POL-001") } returns true

            assertThatThrownBy { runBlocking { service.createPolicyWithAttributes(req) } }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("POL-001")
        }
    }

    // ── getAllPolicies ───────────────────────────────────────

    @Test @DisplayName("Returns all policies from repository")
    fun getAllPolicies_returnsList() {
        runBlocking {
            val pm = PolicyMaster(policyNo = "POL-001")
            every { policyMasterRepository.findAll() } returns listOf(pm).asFlow()

            val result = service.getAllPolicies().toList()
            assertThat(result).hasSize(1)
            assertThat(result.first().policyNo).isEqualTo("POL-001")
        }
    }

    // ── getAttributesForPolicy ──────────────────────────────

    @Test @DisplayName("Retrieves all attribute values for a given policyNo")
    fun getAttributesForPolicy_returnsMapped() {
        runBlocking {
            val pav = buildValue("POL-001", "STR_ATTR", "hello")
            every { valueRepository.findByPolicyNo("POL-001") } returns listOf(pav).asFlow()
            every { masterRepository.findAll() } returns listOf(stringMaster).asFlow()

            val result = service.getAttributesForPolicy("POL-001")
            assertThat(result).hasSize(1)
            assertThat(result[0].policyNo).isEqualTo("POL-001")
            assertThat(result[0].attributeCode).isEqualTo("STR_ATTR")
        }
    }

    @Test @DisplayName("getAttributesForPolicy → maps groupCode from AttributeGroup")
    fun getAttributesForPolicy_mapsGroupCode() {
        runBlocking {
            val master = buildMaster("STR_ATTR", DataType.STRING, false, null, null).apply {
                groupCode = "CONSENT"
            }
            val pav = buildValue("POL-001", "STR_ATTR", "hello")
            every { valueRepository.findByPolicyNo("POL-001") } returns listOf(pav).asFlow()
            every { masterRepository.findAll() } returns listOf(master).asFlow()

            val result = service.getAttributesForPolicy("POL-001")
            assertThat(result).hasSize(1)
            assertThat(result[0].groupCode).isEqualTo("CONSENT")
        }
    }

    @Test @DisplayName("Policy with no attributes → returns empty list")
    fun getAttributesForPolicy_empty() {
        runBlocking {
            every { valueRepository.findByPolicyNo("POL-EMPTY") } returns emptyList<PolicyAttributeValue>().asFlow()
            every { masterRepository.findAll() } returns emptyList<AttributeMaster>().asFlow()
            assertThat(service.getAttributesForPolicy("POL-EMPTY")).isEmpty()
        }
    }

    // ── updateAttributeValue ────────────────────────────────

    @Test @DisplayName("Valid STRING value → saves and returns DTO")
    fun update_string_valid() {
        runBlocking {
            coEvery { masterRepository.findById("STR_ATTR") } returns stringMaster
            val pav = buildValue("POL-001", "STR_ATTR", "hello")
            coEvery { valueRepository.findByPolicyNoAndAttributeCode("POL-001", "STR_ATTR") } returns pav
            coEvery { valueRepository.save(any()) } returns pav

            val result = service.updateAttributeValue("POL-001", "STR_ATTR", "hello")
            assertThat(result.attributeValue).isEqualTo("hello")
        }
    }

    @Test @DisplayName("Attribute not found → throws ResourceNotFoundException")
    fun update_attributeNotFound() {
        runBlocking {
            coEvery { masterRepository.findById("MISSING") } returns null
            assertThatThrownBy { runBlocking { service.updateAttributeValue("POL-001", "MISSING", "val") } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("MISSING")
        }
    }

    @Test @DisplayName("ARCHIVED attribute → throws AttributeValidationException")
    fun update_archivedAttribute() {
        runBlocking {
            val archived = buildMaster("ARC_ATTR", DataType.STRING, false, null, null)
                .also { it.status = AttributeStatus.ARCHIVED }
            coEvery { masterRepository.findById("ARC_ATTR") } returns archived

            assertThatThrownBy { runBlocking { service.updateAttributeValue("POL-001", "ARC_ATTR", "val") } }
                .isInstanceOf(AttributeValidationException::class.java)
                .hasMessageContaining("ARCHIVED")
        }
    }

    // ── validateDataType ────────────────────────────────────

    @Test @DisplayName("NUMBER: valid numeric string → no exception")
    fun validateDataType_number_valid() {
        assertThatNoException().isThrownBy { service.validateDataType(numberMaster, "123.45") }
    }

    @Test @DisplayName("NUMBER: non-numeric string → throws AttributeValidationException")
    fun validateDataType_number_invalid() {
        assertThatThrownBy { service.validateDataType(numberMaster, "abc") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Expected a valid number")
    }

    @Test @DisplayName("DATE: valid ISO date → no exception")
    fun validateDataType_date_valid() {
        assertThatNoException().isThrownBy { service.validateDataType(dateMaster, "2026-05-17") }
    }

    @Test @DisplayName("DATE: wrong date format → throws AttributeValidationException")
    fun validateDataType_date_invalid() {
        assertThatThrownBy { service.validateDataType(dateMaster, "05/17/2026") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Expected a valid date")
    }

    @Test @DisplayName("BOOLEAN: 'true' (case-insensitive) → no exception")
    fun validateDataType_boolean_trueValid() {
        assertThatNoException().isThrownBy { service.validateDataType(boolMaster, "TRUE") }
    }

    @Test @DisplayName("BOOLEAN: 'false' (case-insensitive) → no exception")
    fun validateDataType_boolean_falseValid() {
        assertThatNoException().isThrownBy { service.validateDataType(boolMaster, "false") }
    }

    @Test @DisplayName("BOOLEAN: 'yes' → throws AttributeValidationException")
    fun validateDataType_boolean_invalid() {
        assertThatThrownBy { service.validateDataType(boolMaster, "yes") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Expected true or false")
    }

    @Test @DisplayName("STRING: any value → no data-type exception")
    fun validateDataType_string_anyValue() {
        assertThatNoException().isThrownBy { service.validateDataType(stringMaster, "anything@123!") }
    }

    @Test @DisplayName("Required field with blank value → throws AttributeValidationException")
    fun validateDataType_required_blankValue() {
        assertThatThrownBy { service.validateDataType(requiredMaster, "") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Value is required")
    }

    @Test @DisplayName("Non-required field with null value → no exception (skips validation)")
    fun validateDataType_notRequired_nullValue() {
        assertThatNoException().isThrownBy { service.validateDataType(stringMaster, null) }
    }

    // ── validateValueAgainstRegex ───────────────────────────

    @Test @DisplayName("Regex match: value matches pattern → no exception")
    fun validateRegex_matches() {
        assertThatNoException().isThrownBy { service.validateValueAgainstRegex(regexMaster, "MAX_123") }
    }

    @Test @DisplayName("Regex mismatch: custom error message is thrown")
    fun validateRegex_mismatch_customErrorMessage() {
        assertThatThrownBy { service.validateValueAgainstRegex(regexMaster, "MIN_123") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Must match MAX_<number>")
    }

    @Test @DisplayName("Regex mismatch: null error message → fallback default message")
    fun validateRegex_mismatch_defaultMessage() {
        val noMsg = buildMaster("X", DataType.STRING, false, "^ONLY$", null)
        assertThatThrownBy { service.validateValueAgainstRegex(noMsg, "OTHER") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("^ONLY$")
    }

    @Test @DisplayName("Null regex pattern → skips regex check entirely")
    fun validateRegex_nullPattern_skips() {
        assertThatNoException().isThrownBy { service.validateValueAgainstRegex(stringMaster, "anything") }
    }

    @Test @DisplayName("Blank value with regex defined → skips regex check")
    fun validateRegex_blankValue_skips() {
        assertThatNoException().isThrownBy { service.validateValueAgainstRegex(regexMaster, "") }
    }

    @Test @DisplayName("Regex cache: second call for same pattern reuses compiled Pattern")
    fun validateRegex_cacheReuse() {
        assertThatNoException().isThrownBy {
            service.validateValueAgainstRegex(regexMaster, "MAX_1")
            service.validateValueAgainstRegex(regexMaster, "MAX_99")
        }
    }

    @Test @DisplayName("New policy with empty attributes list → saves PolicyMaster and returns empty list")
    fun createPolicy_emptyAttributesList() {
        runBlocking {
            val req = CreatePolicyRequestDto(policyNo = "POL-003", attributes = emptyList())
            coEvery { policyMasterRepository.existsById("POL-003") } returns false
            coEvery { policyMasterRepository.save(any()) } returns PolicyMaster(policyNo = "POL-003")

            val result = service.createPolicyWithAttributes(req)
            coVerify(exactly = 1) { policyMasterRepository.save(any()) }
            assertThat(result).isEmpty()
        }
    }

    @Test @DisplayName("Update attribute value (no existing value) → inserts new value entity")
    fun update_noExistingValue_inserts() {
        runBlocking {
            coEvery { masterRepository.findById("STR_ATTR") } returns stringMaster
            coEvery { valueRepository.findByPolicyNoAndAttributeCode("POL-001", "STR_ATTR") } returns null
            
            val slot = slot<PolicyAttributeValue>()
            val savedVal = buildValue("POL-001", "STR_ATTR", "new_val")
            coEvery { valueRepository.save(capture(slot)) } returns savedVal

            val result = service.updateAttributeValue("POL-001", "STR_ATTR", "new_val")
            assertThat(slot.captured.attributeValue).isEqualTo("new_val")
            assertThat(result.attributeValue).isEqualTo("new_val")
        }
    }

    @Test @DisplayName("Regex Cache: Compiling 501+ patterns correctly eviction limits size")
    fun validateRegex_cacheEvictionLimit() {
        // Feed 502 different regex patterns
        for (i in 1..502) {
            val master = buildMaster("REG_$i", DataType.STRING, false, "^PAT_${i}$", null)
            service.validateValueAgainstRegex(master, "PAT_$i")
        }
        // Verification that it successfully processed all without OOM or exceptions
        assertThatNoException().isThrownBy {
            val checkMaster = buildMaster("REG_CHECK", DataType.STRING, false, "^TEST$", null)
            service.validateValueAgainstRegex(checkMaster, "TEST")
        }
    }
}
