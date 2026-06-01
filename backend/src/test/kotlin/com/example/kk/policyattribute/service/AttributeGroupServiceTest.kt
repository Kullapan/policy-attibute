package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.dto.AttributeGroupDto
import com.example.kk.policyattribute.exception.AttributeValidationException
import com.example.kk.policyattribute.exception.ResourceNotFoundException
import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.repository.AttributeGroupRepository
import io.mockk.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("AttributeGroupService Unit Tests")
class AttributeGroupServiceTest {

    private val repository = mockk<AttributeGroupRepository>()
    private val service = AttributeGroupService(repository)

    private lateinit var activeGroup: AttributeGroup

    @BeforeEach
    fun setUp() {
        activeGroup = AttributeGroup(
            code = "CONSENT",
            displayNameEn = "Consent",
            displayNameTh = "ความยินยอม (Consent)",
            displayOrder = 1,
            status = AttributeStatus.ACTIVE,
            version = 1L,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            createdBy = "system"
        )
    }

    @Test @DisplayName("listActiveGroups → returns only active groups ordered by displayOrder")
    fun listActiveGroups_returnsActive() {
        runBlocking {
            every { repository.findByStatusOrderByDisplayOrderAsc(AttributeStatus.ACTIVE) } returns listOf(activeGroup).asFlow()

            val result = service.listActiveGroups().toList()

            assertThat(result).hasSize(1)
            val dto = result.first()
            assertThat(dto.code).isEqualTo("CONSENT")
            assertThat(dto.displayNameEn).isEqualTo("Consent")
            assertThat(dto.displayNameTh).isEqualTo("ความยินยอม (Consent)")
            assertThat(dto.displayOrder).isEqualTo(1)
            assertThat(dto.status).isEqualTo(AttributeStatus.ACTIVE)
        }
    }

    @Test @DisplayName("listGroups(includeArchived = true) → returns all groups")
    fun listGroups_includeArchived() {
        runBlocking {
            val archivedGroup = AttributeGroup(
                code = "OTHER",
                displayNameEn = "Other",
                displayNameTh = "อื่นๆ",
                displayOrder = 2,
                status = AttributeStatus.ARCHIVED
            )
            every { repository.findAllByOrderByDisplayOrderAsc() } returns listOf(activeGroup, archivedGroup).asFlow()

            val result = service.listGroups(true).toList()

            assertThat(result).hasSize(2)
            assertThat(result[0].code).isEqualTo("CONSENT")
            assertThat(result[1].code).isEqualTo("OTHER")
        }
    }

    @Test @DisplayName("listGroups(includeArchived = false) → returns active groups")
    fun listGroups_excludeArchived() {
        runBlocking {
            every { repository.findByStatusOrderByDisplayOrderAsc(AttributeStatus.ACTIVE) } returns listOf(activeGroup).asFlow()

            val result = service.listGroups(false).toList()

            assertThat(result).hasSize(1)
            assertThat(result.first().code).isEqualTo("CONSENT")
        }
    }

    @Test @DisplayName("getByCode → returns group when found")
    fun getByCode_found() {
        runBlocking {
            coEvery { repository.findById("CONSENT") } returns activeGroup

            val result = service.getByCode("CONSENT")

            assertThat(result.code).isEqualTo("CONSENT")
            assertThat(result.displayNameEn).isEqualTo("Consent")
        }
    }

    @Test @DisplayName("getByCode → throws ResourceNotFoundException when not found")
    fun getByCode_notFound() {
        runBlocking {
            coEvery { repository.findById("MISSING") } returns null

            assertThatThrownBy { runBlocking { service.getByCode("MISSING") } }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("MISSING")
        }
    }

    @Test @DisplayName("create → saves new active group")
    fun create_success() {
        runBlocking {
            val dto = AttributeGroupDto(
                code = "NEW_GROUP",
                displayNameEn = "New Group",
                displayNameTh = "กลุ่มใหม่",
                displayOrder = 2
            )
            coEvery { repository.existsById("NEW_GROUP") } returns false
            
            val slot = slot<AttributeGroup>()
            val savedEntity = AttributeGroup(
                code = "NEW_GROUP",
                displayNameEn = "New Group",
                displayNameTh = "กลุ่มใหม่",
                displayOrder = 2,
                status = AttributeStatus.ACTIVE
            )
            coEvery { repository.save(capture(slot)) } returns savedEntity

            val result = service.create(dto)

            assertThat(slot.captured.code).isEqualTo("NEW_GROUP")
            assertThat(slot.captured.status).isEqualTo(AttributeStatus.ACTIVE)
            assertThat(result.code).isEqualTo("NEW_GROUP")
        }
    }

    @Test @DisplayName("create → throws AttributeValidationException if code already exists")
    fun create_alreadyExists() {
        runBlocking {
            val dto = AttributeGroupDto(code = "CONSENT", displayNameEn = "Consent")
            coEvery { repository.existsById("CONSENT") } returns true

            assertThatThrownBy { runBlocking { service.create(dto) } }
                .isInstanceOf(AttributeValidationException::class.java)
                .hasMessageContaining("already exists")
        }
    }

    @Test @DisplayName("update → updates fields and version")
    fun update_success() {
        runBlocking {
            coEvery { repository.findById("CONSENT") } returns activeGroup
            
            val dto = AttributeGroupDto(
                code = "CONSENT",
                displayNameEn = "Updated Consent",
                displayNameTh = "ความยินยอมใหม่",
                displayOrder = 5,
                status = AttributeStatus.ACTIVE,
                version = 2L
            )
            val slot = slot<AttributeGroup>()
            coEvery { repository.save(capture(slot)) } returns activeGroup // mock save return

            service.update("CONSENT", dto)

            assertThat(slot.captured.displayNameEn).isEqualTo("Updated Consent")
            assertThat(slot.captured.displayNameTh).isEqualTo("ความยินยอมใหม่")
            assertThat(slot.captured.displayOrder).isEqualTo(5)
            assertThat(slot.captured.version).isEqualTo(2L)
        }
    }

    @Test @DisplayName("update → throws ResourceNotFoundException if group not found")
    fun update_notFound() {
        runBlocking {
            val dto = AttributeGroupDto(code = "MISSING", displayNameEn = "Missing")
            coEvery { repository.findById("MISSING") } returns null

            assertThatThrownBy { runBlocking { service.update("MISSING", dto) } }
                .isInstanceOf(ResourceNotFoundException::class.java)
        }
    }

    @Test @DisplayName("softDelete → updates status to ARCHIVED")
    fun softDelete_success() {
        runBlocking {
            coEvery { repository.findById("CONSENT") } returns activeGroup
            val slot = slot<AttributeGroup>()
            coEvery { repository.save(capture(slot)) } returns activeGroup

            service.softDelete("CONSENT")

            assertThat(slot.captured.status).isEqualTo(AttributeStatus.ARCHIVED)
        }
    }

    @Test @DisplayName("softDelete → throws ResourceNotFoundException if group not found")
    fun softDelete_notFound() {
        runBlocking {
            coEvery { repository.findById("MISSING") } returns null

            assertThatThrownBy { runBlocking { service.softDelete("MISSING") } }
                .isInstanceOf(ResourceNotFoundException::class.java)
        }
    }
}
