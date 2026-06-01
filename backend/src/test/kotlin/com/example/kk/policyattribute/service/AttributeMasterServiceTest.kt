package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeMaster
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.model.DataType
import com.example.kk.policyattribute.repository.AttributeMasterRepository
import com.example.kk.policyattribute.repository.AttributeGroupRepository
import io.mockk.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("AttributeMasterService Unit Tests")
class AttributeMasterServiceTest {

    private val repository = mockk<AttributeMasterRepository>()
    private val groupRepository = mockk<AttributeGroupRepository>()
    private val service = AttributeMasterService(repository, groupRepository)

    private lateinit var active: AttributeMaster
    private lateinit var validDto: AttributeMasterDto

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
        runBlocking {
            every { repository.findAll() } returns listOf(active).asFlow()
            val result = service.listAttributes(null, null).toList()
            assertThat(result).hasSize(1)
            assertThat(result.first().code).isEqualTo("MAX_LIMIT")
        }
    }

    @Test @DisplayName("Blank search → treated as no search filter")
    fun listAll_blankSearch() {
        runBlocking {
            every { repository.findAll() } returns listOf(active).asFlow()
            service.listAttributes("   ", null).toList()
            verify(exactly = 1) { repository.findAll() }
            verify(exactly = 0) { repository.searchByNameOrCode(any()) }
        }
    }

    @Test @DisplayName("Status filter only → uses findByStatus")
    fun list_statusFilter() {
        runBlocking {
            every { repository.findByStatus(AttributeStatus.ACTIVE) } returns listOf(active).asFlow()
            service.listAttributes(null, AttributeStatus.ACTIVE).toList()
            verify(exactly = 1) { repository.findByStatus(AttributeStatus.ACTIVE) }
        }
    }

    @Test @DisplayName("Search only → uses searchByNameOrCode with trimmed input")
    fun list_searchOnly() {
        runBlocking {
            every { repository.searchByNameOrCode("limit") } returns listOf(active).asFlow()
            service.listAttributes("  limit  ", null).toList()
            verify(exactly = 1) { repository.searchByNameOrCode("limit") }
        }
    }

    @Test @DisplayName("Search + status → uses searchAttributes")
    fun list_searchAndStatus() {
        runBlocking {
            every { repository.searchAttributes("limit", "ACTIVE") } returns listOf(active).asFlow()
            service.listAttributes("limit", AttributeStatus.ACTIVE).toList()
            verify(exactly = 1) { repository.searchAttributes("limit", "ACTIVE") }
        }
    }

    // ── getByCode ───────────────────────────────────────────

    @Test @DisplayName("Existing code → returns DTO with all fields mapped")
    fun getByCode_found() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            val dto = service.getByCode("MAX_LIMIT")
            assertThat(dto.code).isEqualTo("MAX_LIMIT")
            assertThat(dto.dataType).isEqualTo(DataType.NUMBER)
        }
    }

    @Test @DisplayName("Unknown code → throws ResourceNotFoundException")
    fun getByCode_notFound() {
        runBlocking {
            coEvery { repository.findById("UNKNOWN") } returns null
            assertThatThrownBy { runBlocking { service.getByCode("UNKNOWN") } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("UNKNOWN")
        }
    }

    // ── create ──────────────────────────────────────────────

    @Test @DisplayName("New code with valid regex → saves with ACTIVE status")
    fun create_success() {
        runBlocking {
            coEvery { repository.existsById("MAX_LIMIT") } returns false
            coEvery { groupRepository.existsById(any()) } returns true
            val slot = slot<AttributeMaster>()
            coEvery { repository.save(capture(slot)) } returns active

            service.create(validDto)

            assertThat(slot.captured.status).isEqualTo(AttributeStatus.ACTIVE)
        }
    }

    @Test @DisplayName("New code with groupCode → saves with associated AttributeGroup")
    fun create_withGroupCode_success() {
        runBlocking {
            val dto = validDto.copy(groupCode = "CONSENT")
            coEvery { repository.existsById("MAX_LIMIT") } returns false
            coEvery { groupRepository.existsById("CONSENT") } returns true

            val activeWithGroup = active.apply { groupCode = "CONSENT" }
            val slot = slot<AttributeMaster>()
            coEvery { repository.save(capture(slot)) } returns activeWithGroup

            val result = service.create(dto)

            assertThat(slot.captured.groupCode).isEqualTo("CONSENT")
            assertThat(result.groupCode).isEqualTo("CONSENT")
        }
    }

    @Test @DisplayName("Duplicate code → throws AttributeValidationException, never saves")
    fun create_duplicateCode() {
        runBlocking {
            coEvery { repository.existsById("MAX_LIMIT") } returns true
            assertThatThrownBy { runBlocking { service.create(validDto) } }
                .isInstanceOf(AttributeValidationException::class.java)
                .hasMessageContaining("already exists")
            coVerify(exactly = 0) { repository.save(any()) }
        }
    }

    @Test @DisplayName("New code with non-existent groupCode → throws ResourceNotFoundException")
    fun create_nonExistentGroupCode() {
        runBlocking {
            val dto = validDto.copy(groupCode = "UNKNOWN_GROUP")
            coEvery { repository.existsById("MAX_LIMIT") } returns false
            coEvery { groupRepository.existsById("UNKNOWN_GROUP") } returns false

            assertThatThrownBy { runBlocking { service.create(dto) } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("UNKNOWN_GROUP")
            coVerify(exactly = 0) { repository.save(any()) }
        }
    }

    @Test @DisplayName("Invalid regex syntax → throws AttributeValidationException before save")
    fun create_invalidRegex() {
        runBlocking {
            val bad = AttributeMasterDto(code = "MAX_LIMIT", displayName = "X", dataType = DataType.STRING, regexPattern = "[A-Z")
            coEvery { repository.existsById("MAX_LIMIT") } returns false
            assertThatThrownBy { runBlocking { service.create(bad) } }
                .isInstanceOf(AttributeValidationException::class.java)
                .hasMessageContaining("Invalid regex pattern")
            coVerify(exactly = 0) { repository.save(any()) }
        }
    }

    @Test @DisplayName("Null regex → skips validation and saves")
    fun create_nullRegex() {
        runBlocking {
            val dto = AttributeMasterDto(code = "NO_REGEX", displayName = "X", dataType = DataType.STRING)
            val saved = AttributeMaster(code = "NO_REGEX", displayName = "X", dataType = DataType.STRING, status = AttributeStatus.ACTIVE)
            coEvery { repository.existsById("NO_REGEX") } returns false
            coEvery { repository.save(any()) } returns saved
            assertThatNoException().isThrownBy { runBlocking { service.create(dto) } }
        }
    }

    @Test @DisplayName("Blank regex string → skips validation and saves")
    fun create_blankRegex() {
        runBlocking {
            val dto = AttributeMasterDto(code = "BLANK", displayName = "X", dataType = DataType.STRING, regexPattern = "   ")
            val saved = AttributeMaster(code = "BLANK", displayName = "X", dataType = DataType.STRING, status = AttributeStatus.ACTIVE)
            coEvery { repository.existsById("BLANK") } returns false
            coEvery { repository.save(any()) } returns saved
            assertThatNoException().isThrownBy { runBlocking { service.create(dto) } }
        }
    }

    // ── update ──────────────────────────────────────────────

    @Test @DisplayName("Existing code + valid data → updates display name, version")
    fun update_success() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            val slot = slot<AttributeMaster>()
            coEvery { repository.save(capture(slot)) } returns active
            val dto = AttributeMasterDto(displayName = "Updated Name", dataType = DataType.NUMBER, regexPattern = "^\\d+$", version = 2L)
            service.update("MAX_LIMIT", dto)
            assertThat(slot.captured.displayName).isEqualTo("Updated Name")
            assertThat(slot.captured.version).isEqualTo(2L)
        }
    }

    @Test @DisplayName("Update groupCode → updates attributeGroup association")
    fun update_groupCode_success() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            coEvery { groupRepository.existsById("CONSENT") } returns true
            val slot = slot<AttributeMaster>()
            coEvery { repository.save(capture(slot)) } returns active

            val dto = AttributeMasterDto(displayName = "Updated Name", groupCode = "CONSENT")
            service.update("MAX_LIMIT", dto)

            assertThat(slot.captured.groupCode).isEqualTo("CONSENT")
        }
    }

    @Test @DisplayName("Update to non-existent groupCode → throws ResourceNotFoundException")
    fun update_nonExistentGroupCode() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            coEvery { groupRepository.existsById("UNKNOWN_GROUP") } returns false

            val dto = AttributeMasterDto(displayName = "Updated Name", groupCode = "UNKNOWN_GROUP")
            assertThatThrownBy { runBlocking { service.update("MAX_LIMIT", dto) } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("UNKNOWN_GROUP")
            coVerify(exactly = 0) { repository.save(any()) }
        }
    }

    @Test @DisplayName("Non-existent code on update → throws ResourceNotFoundException")
    fun update_notFound() {
        runBlocking {
            coEvery { repository.findById("MISSING") } returns null
            assertThatThrownBy { runBlocking { service.update("MISSING", validDto) } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("MISSING")
        }
    }

    @Test @DisplayName("Invalid regex on update → throws AttributeValidationException, never saves")
    fun update_invalidRegex() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            val bad = AttributeMasterDto(displayName = "X", dataType = DataType.STRING, regexPattern = "(unclosed")
            assertThatThrownBy { runBlocking { service.update("MAX_LIMIT", bad) } }
                .isInstanceOf(AttributeValidationException::class.java)
                .hasMessageContaining("Invalid regex pattern")
            coVerify(exactly = 0) { repository.save(any()) }
        }
    }

    @Test @DisplayName("Null version on update → entity version remains unchanged")
    fun update_nullVersionPreservesExistingVersion() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            val slot = slot<AttributeMaster>()
            coEvery { repository.save(capture(slot)) } returns active
            val dto = AttributeMasterDto(displayName = "X", dataType = DataType.STRING, version = null)
            service.update("MAX_LIMIT", dto)
            assertThat(slot.captured.version).isEqualTo(1L)
        }
    }

    // ── softDelete ──────────────────────────────────────────

    @Test @DisplayName("Existing code → sets status ARCHIVED and saves")
    fun softDelete_archives() {
        runBlocking {
            coEvery { repository.findById("MAX_LIMIT") } returns active
            val slot = slot<AttributeMaster>()
            coEvery { repository.save(capture(slot)) } returns active
            service.softDelete("MAX_LIMIT")
            assertThat(slot.captured.status).isEqualTo(AttributeStatus.ARCHIVED)
        }
    }

    @Test @DisplayName("Non-existent code on delete → throws ResourceNotFoundException")
    fun softDelete_notFound() {
        runBlocking {
            coEvery { repository.findById("GHOST") } returns null
            assertThatThrownBy { runBlocking { service.softDelete("GHOST") } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("GHOST")
        }
    }
}
