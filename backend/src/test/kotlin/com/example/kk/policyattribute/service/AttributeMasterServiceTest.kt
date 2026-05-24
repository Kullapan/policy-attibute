package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("AttributeMasterService Unit Tests")
class AttributeMasterServiceTest {

    @Mock lateinit var repository: AttributeMasterRepository
    @Mock lateinit var groupRepository: com.example.kk.policyattribute.repository.AttributeGroupRepository
    @InjectMocks lateinit var service: AttributeMasterService

    lateinit var active: AttributeMaster
    lateinit var validDto: AttributeMasterDto

    @BeforeEach
    fun setUp() {
        active = AttributeMaster(
            code = "MAX_LIMIT", displayName = "Maximum Limit",
            dataType = DataType.NUMBER, status = AttributeStatus.ACTIVE,
            isRequired = true, regexPattern = "^\\d+$",
            regexErrorMsg = "Must be a positive integer",
            version = 1L, createdAt = Instant.now(), updatedAt = Instant.now(),
            createdBy = "system"
        )
        validDto = AttributeMasterDto(
            code = "MAX_LIMIT", displayName = "Maximum Limit",
            dataType = DataType.NUMBER, isRequired = true,
            regexPattern = "^\\d+$", regexErrorMsg = "Must be a positive integer"
        )
    }

    // ── listAttributes ──────────────────────────────────────

    @Test @DisplayName("No filters → returns all attributes")
    fun listAll_noFilters() {
        whenever(repository.findAll()).thenReturn(listOf(active))
        val result = service.listAttributes(null, null)
        assertThat(result).hasSize(1)
        assertThat(result.first().code).isEqualTo("MAX_LIMIT")
    }

    @Test @DisplayName("Blank search → treated as no search filter")
    fun listAll_blankSearch() {
        whenever(repository.findAll()).thenReturn(listOf(active))
        service.listAttributes("   ", null)
        verify(repository).findAll()
        verify(repository, never()).searchByNameOrCode(any())
    }

    @Test @DisplayName("Status filter only → uses findByStatus")
    fun list_statusFilter() {
        whenever(repository.findByStatus(AttributeStatus.ACTIVE)).thenReturn(listOf(active))
        service.listAttributes(null, AttributeStatus.ACTIVE)
        verify(repository).findByStatus(AttributeStatus.ACTIVE)
    }

    @Test @DisplayName("Search only → uses searchByNameOrCode with trimmed input")
    fun list_searchOnly() {
        whenever(repository.searchByNameOrCode("limit")).thenReturn(listOf(active))
        service.listAttributes("  limit  ", null)
        verify(repository).searchByNameOrCode("limit")
    }

    @Test @DisplayName("Search + status → uses searchAttributes")
    fun list_searchAndStatus() {
        whenever(repository.searchAttributes("limit", AttributeStatus.ACTIVE)).thenReturn(listOf(active))
        service.listAttributes("limit", AttributeStatus.ACTIVE)
        verify(repository).searchAttributes("limit", AttributeStatus.ACTIVE)
    }

    // ── getByCode ───────────────────────────────────────────

    @Test @DisplayName("Existing code → returns DTO with all fields mapped")
    fun getByCode_found() {
        whenever(repository.findById("MAX_LIMIT")).thenReturn(Optional.of(active))
        val dto = service.getByCode("MAX_LIMIT")
        assertThat(dto.code).isEqualTo("MAX_LIMIT")
        assertThat(dto.dataType).isEqualTo(DataType.NUMBER)
    }

    @Test @DisplayName("Unknown code → throws ResourceNotFoundException")
    fun getByCode_notFound() {
        whenever(repository.findById("UNKNOWN")).thenReturn(Optional.empty())
        assertThatThrownBy { service.getByCode("UNKNOWN") }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("UNKNOWN")
    }

    // ── create ──────────────────────────────────────────────

    @Test @DisplayName("New code with valid regex → saves with ACTIVE status")
    fun create_success() {
        whenever(repository.existsById("MAX_LIMIT")).thenReturn(false)
        whenever(repository.save(any<AttributeMaster>())).thenReturn(active)
        service.create(validDto)
        val cap = ArgumentCaptor.forClass(AttributeMaster::class.java)
        verify(repository).save(cap.capture())
        assertThat(cap.value.status).isEqualTo(AttributeStatus.ACTIVE)
    }

    @Test @DisplayName("New code with groupCode → saves with associated AttributeGroup")
    fun create_withGroupCode_success() {
        val dto = validDto.copy(groupCode = "CONSENT")
        val group = AttributeGroup(code = "CONSENT", displayNameEn = "Consent", displayNameTh = "ความยินยอม")
        whenever(repository.existsById("MAX_LIMIT")).thenReturn(false)
        whenever(groupRepository.findById("CONSENT")).thenReturn(Optional.of(group))
        
        val activeWithGroup = active.apply { attributeGroup = group }
        whenever(repository.save(any<AttributeMaster>())).thenReturn(activeWithGroup)
        
        val result = service.create(dto)
        
        val cap = ArgumentCaptor.forClass(AttributeMaster::class.java)
        verify(repository).save(cap.capture())
        assertThat(cap.value.attributeGroup).isEqualTo(group)
        assertThat(result.groupCode).isEqualTo("CONSENT")
    }

    @Test @DisplayName("Duplicate code → throws AttributeValidationException, never saves")
    fun create_duplicateCode() {
        whenever(repository.existsById("MAX_LIMIT")).thenReturn(true)
        assertThatThrownBy { service.create(validDto) }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("already exists")
        verify(repository, never()).save(any())
    }

    @Test @DisplayName("Invalid regex syntax → throws AttributeValidationException before save")
    fun create_invalidRegex() {
        val bad = AttributeMasterDto(code = "MAX_LIMIT", displayName = "X", dataType = DataType.STRING, regexPattern = "[A-Z")
        whenever(repository.existsById("MAX_LIMIT")).thenReturn(false)
        assertThatThrownBy { service.create(bad) }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Invalid regex pattern")
        verify(repository, never()).save(any())
    }

    @Test @DisplayName("Null regex → skips validation and saves")
    fun create_nullRegex() {
        val dto = AttributeMasterDto(code = "NO_REGEX", displayName = "X", dataType = DataType.STRING)
        val saved = AttributeMaster(code = "NO_REGEX", displayName = "X", dataType = DataType.STRING, status = AttributeStatus.ACTIVE)
        whenever(repository.existsById("NO_REGEX")).thenReturn(false)
        whenever(repository.save(any<AttributeMaster>())).thenReturn(saved)
        assertThatNoException().isThrownBy { service.create(dto) }
    }

    @Test @DisplayName("Blank regex string → skips validation and saves")
    fun create_blankRegex() {
        val dto = AttributeMasterDto(code = "BLANK", displayName = "X", dataType = DataType.STRING, regexPattern = "   ")
        val saved = AttributeMaster(code = "BLANK", displayName = "X", dataType = DataType.STRING, status = AttributeStatus.ACTIVE)
        whenever(repository.existsById("BLANK")).thenReturn(false)
        whenever(repository.save(any<AttributeMaster>())).thenReturn(saved)
        assertThatNoException().isThrownBy { service.create(dto) }
    }

    // ── update ──────────────────────────────────────────────

    @Test @DisplayName("Existing code + valid data → updates display name, version")
    fun update_success() {
        whenever(repository.findById("MAX_LIMIT")).thenReturn(Optional.of(active))
        whenever(repository.save(any<AttributeMaster>())).thenReturn(active)
        val dto = AttributeMasterDto(displayName = "Updated Name", dataType = DataType.NUMBER, regexPattern = "^\\d+$", version = 2L)
        service.update("MAX_LIMIT", dto)
        assertThat(active.displayName).isEqualTo("Updated Name")
        assertThat(active.version).isEqualTo(2L)
    }

    @Test @DisplayName("Update groupCode → updates attributeGroup association")
    fun update_groupCode_success() {
        val group = AttributeGroup(code = "CONSENT", displayNameEn = "Consent", displayNameTh = "ความยินยอม")
        whenever(repository.findById("MAX_LIMIT")).thenReturn(Optional.of(active))
        whenever(groupRepository.findById("CONSENT")).thenReturn(Optional.of(group))
        whenever(repository.save(any<AttributeMaster>())).thenReturn(active)
        
        val dto = AttributeMasterDto(displayName = "Updated Name", groupCode = "CONSENT")
        service.update("MAX_LIMIT", dto)
        
        assertThat(active.attributeGroup).isEqualTo(group)
    }

    @Test @DisplayName("Non-existent code on update → throws ResourceNotFoundException")
    fun update_notFound() {
        whenever(repository.findById("MISSING")).thenReturn(Optional.empty())
        assertThatThrownBy { service.update("MISSING", validDto) }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("MISSING")
    }

    @Test @DisplayName("Invalid regex on update → throws AttributeValidationException, never saves")
    fun update_invalidRegex() {
        whenever(repository.findById("MAX_LIMIT")).thenReturn(Optional.of(active))
        val bad = AttributeMasterDto(displayName = "X", dataType = DataType.STRING, regexPattern = "(unclosed")
        assertThatThrownBy { service.update("MAX_LIMIT", bad) }
            .isInstanceOf(AttributeValidationException::class.java)
            .hasMessageContaining("Invalid regex pattern")
        verify(repository, never()).save(any())
    }

    @Test @DisplayName("Null version on update → entity version remains unchanged")
    fun update_nullVersionPreservesExistingVersion() {
        whenever(repository.findById("MAX_LIMIT")).thenReturn(Optional.of(active))
        whenever(repository.save(any<AttributeMaster>())).thenReturn(active)
        val dto = AttributeMasterDto(displayName = "X", dataType = DataType.STRING, version = null)
        service.update("MAX_LIMIT", dto)
        assertThat(active.version).isEqualTo(1L)
    }

    // ── softDelete ──────────────────────────────────────────

    @Test @DisplayName("Existing code → sets status ARCHIVED and saves")
    fun softDelete_archives() {
        whenever(repository.findById("MAX_LIMIT")).thenReturn(Optional.of(active))
        service.softDelete("MAX_LIMIT")
        val cap = ArgumentCaptor.forClass(AttributeMaster::class.java)
        verify(repository).save(cap.capture())
        assertThat(cap.value.status).isEqualTo(AttributeStatus.ARCHIVED)
    }

    @Test @DisplayName("Non-existent code on delete → throws ResourceNotFoundException")
    fun softDelete_notFound() {
        whenever(repository.findById("GHOST")).thenReturn(Optional.empty())
        assertThatThrownBy { service.softDelete("GHOST") }
            .isInstanceOf(ResourceNotFoundException::class.java)
            .hasMessageContaining("GHOST")
    }
}
