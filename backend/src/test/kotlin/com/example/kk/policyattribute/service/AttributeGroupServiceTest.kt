package com.example.kk.policyattribute.service

import com.example.kk.policyattribute.model.AttributeGroup
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.repository.AttributeGroupRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@DisplayName("AttributeGroupService Unit Tests")
class AttributeGroupServiceTest {

    @Mock lateinit var repository: AttributeGroupRepository
    @InjectMocks lateinit var service: AttributeGroupService

    lateinit var activeGroup: AttributeGroup

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
        whenever(repository.findByStatusOrderByDisplayOrderAsc(AttributeStatus.ACTIVE))
            .thenReturn(listOf(activeGroup))

        val result = service.listActiveGroups()

        assertThat(result).hasSize(1)
        val dto = result.first()
        assertThat(dto.code).isEqualTo("CONSENT")
        assertThat(dto.displayNameEn).isEqualTo("Consent")
        assertThat(dto.displayNameTh).isEqualTo("ความยินยอม (Consent)")
        assertThat(dto.displayOrder).isEqualTo(1)
        assertThat(dto.status).isEqualTo(AttributeStatus.ACTIVE)
    }
}
