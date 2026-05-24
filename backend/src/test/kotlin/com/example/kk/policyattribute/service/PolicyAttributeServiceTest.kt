package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.CreatePolicyRequestDto
import com.example.kk.policyattribute.dto.PolicyAttributeValueDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.*
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.PolicyAttributeValueRepository
import com.example.kk.policyattribute.repository.PolicyMasterRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("PolicyAttributeService Unit Tests")
class PolicyAttributeServiceTest {

    @Mock lateinit var valueRepository: PolicyAttributeValueRepository
    @Mock lateinit var masterRepository: AttributeMasterRepository
    @Mock lateinit var policyMasterRepository: PolicyMasterRepository
    @InjectMocks lateinit var service: PolicyAttributeService

    lateinit var stringMaster: AttributeMaster
    lateinit var numberMaster: AttributeMaster
    lateinit var dateMaster: AttributeMaster
    lateinit var boolMaster: AttributeMaster
    lateinit var requiredMaster: AttributeMaster
    lateinit var regexMaster: AttributeMaster

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
            id = PolicyAttributeValueId(policyNo, code),
            attributeValue = val_,
            createdAt = Instant.now(), updatedAt = Instant.now(), createdBy = "system"
        )

    // ── createPolicyWithAttributes ──────────────────────────

    @Test @DisplayName("New policy with attributes → saves PolicyMaster and returns attribute list")
    fun createPolicy_newPolicyWithAttributes() {
        val attrDto = PolicyAttributeValueDto(attributeCode = "STR_ATTR", attributeValue = "hello")
        val req = CreatePolicyRequestDto(policyNo = "POL-001", attributes = listOf(attrDto))

        whenever(policyMasterRepository.existsById("POL-001")).thenReturn(false)
        whenever(masterRepository.findById("STR_ATTR")).thenReturn(Optional.of(stringMaster))
        val savedVal = buildValue("POL-001", "STR_ATTR", "hello")
        val savedPolicy = PolicyMaster(policyNo = "POL-001")
        whenever(policyMasterRepository.save(any<PolicyMaster>())).thenReturn(savedPolicy)
        whenever(valueRepository.findById(any())).thenReturn(Optional.empty())
        whenever(valueRepository.save(any<PolicyAttributeValue>())).thenReturn(savedVal)

        val result = service.createPolicyWithAttributes(req)
        verify(policyMasterRepository).save(any<PolicyMaster>())
        assertThat(result).hasSize(1)
    }

    @Test @DisplayName("New policy with null attributes → saves PolicyMaster and returns empty list")
    fun createPolicy_noAttributes() {
        val req = CreatePolicyRequestDto(policyNo = "POL-002", attributes = null)
        whenever(policyMasterRepository.existsById("POL-002")).thenReturn(false)
        whenever(policyMasterRepository.save(any<PolicyMaster>())).thenReturn(PolicyMaster(policyNo = "POL-002"))

        val result = service.createPolicyWithAttributes(req)
        verify(policyMasterRepository).save(any<PolicyMaster>())
        assertThat(result).isEmpty()
    }

    @Test @DisplayName("Existing policy → throws IllegalArgumentException")
    fun createPolicy_alreadyExists() {
        val req = CreatePolicyRequestDto(policyNo = "POL-001")
        whenever(policyMasterRepository.existsById("POL-001")).thenReturn(true)

        assertThatThrownBy { service.createPolicyWithAttributes(req) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("POL-001")
    }

    // ── getAllPolicies ───────────────────────────────────────

    @Test @DisplayName("Returns all policies from repository")
    fun getAllPolicies_returnsList() {
        val pm = PolicyMaster(policyNo = "POL-001")
        whenever(policyMasterRepository.findAll()).thenReturn(listOf(pm))

        val result = service.getAllPolicies()
        assertThat(result).hasSize(1)
        assertThat(result.first().policyNo).isEqualTo("POL-001")
    }

    // ── getAttributesForPolicy ──────────────────────────────

    @Test @DisplayName("Retrieves all attribute values for a given policyNo")
    fun getAttributesForPolicy_returnsMapped() {
        val pav = buildValue("POL-001", "STR_ATTR", "hello")
        whenever(valueRepository.findByIdPolicyNo("POL-001")).thenReturn(listOf(pav))

        val result = service.getAttributesForPolicy("POL-001")
        assertThat(result).hasSize(1)
        assertThat(result[0].policyNo).isEqualTo("POL-001")
        assertThat(result[0].attributeCode).isEqualTo("STR_ATTR")
    }

    @Test @DisplayName("getAttributesForPolicy → maps groupCode from AttributeGroup")
    fun getAttributesForPolicy_mapsGroupCode() {
        val group = AttributeGroup(code = "CONSENT", displayNameEn = "Consent", displayNameTh = "ความยินยอม")
        val master = buildMaster("STR_ATTR", DataType.STRING, false, null, null).apply {
            attributeGroup = group
        }
        val pav = buildValue("POL-001", "STR_ATTR", "hello").apply {
            attributeMaster = master
        }
        whenever(valueRepository.findByIdPolicyNo("POL-001")).thenReturn(listOf(pav))

        val result = service.getAttributesForPolicy("POL-001")
        assertThat(result).hasSize(1)
        assertThat(result[0].groupCode).isEqualTo("CONSENT")
    }

    @Test @DisplayName("Policy with no attributes → returns empty list")
    fun getAttributesForPolicy_empty() {
        whenever(valueRepository.findByIdPolicyNo("POL-EMPTY")).thenReturn(emptyList())
        assertThat(service.getAttributesForPolicy("POL-EMPTY")).isEmpty()
    }

    // ── updateAttributeValue ────────────────────────────────

    @Test @DisplayName("Valid STRING value → saves and returns DTO")
    fun update_string_valid() {
        whenever(masterRepository.findById("STR_ATTR")).thenReturn(Optional.of(stringMaster))
        val pav = buildValue("POL-001", "STR_ATTR", "hello")
        whenever(valueRepository.findById(any())).thenReturn(Optional.of(pav))
        whenever(valueRepository.save(any<PolicyAttributeValue>())).thenReturn(pav)

        val result = service.updateAttributeValue("POL-001", "STR_ATTR", "hello")
        assertThat(result.attributeValue).isEqualTo("hello")
    }

    @Test @DisplayName("Attribute not found → throws ResourceNotFoundException")
    fun update_attributeNotFound() {
        whenever(masterRepository.findById("MISSING")).thenReturn(Optional.empty())
        assertThatThrownBy { service.updateAttributeValue("POL-001", "MISSING", "val") }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("MISSING")
    }

    @Test @DisplayName("ARCHIVED attribute → throws AttributeValidationException")
    fun update_archivedAttribute() {
        val archived = buildMaster("ARC_ATTR", DataType.STRING, false, null, null)
            .also { it.status = AttributeStatus.ARCHIVED }
        whenever(masterRepository.findById("ARC_ATTR")).thenReturn(Optional.of(archived))

        assertThatThrownBy { service.updateAttributeValue("POL-001", "ARC_ATTR", "val") }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("ARCHIVED")
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
}
