package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.AttributeMasterDto
import com.example.kk.policyattribute.model.AttributeStatus
import com.example.kk.policyattribute.service.AttributeMasterService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlinx.coroutines.flow.Flow

/**
 * REST controller for the Attribute Dictionary (Master table CRUD).
 */
@RestController
@RequestMapping("/api/v1/attributes")
class AttributeMasterController(
    private val service: AttributeMasterService
) {

    /**
     * List all attributes with optional search and status filter.
     */
    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) status: AttributeStatus?
    ): Flow<AttributeMasterDto> =
        service.listAttributes(search, status)

    /**
     * Get a single attribute by its code.
     */
    @GetMapping("/{code}")
    suspend fun getByCode(@PathVariable code: String): ResponseEntity<AttributeMasterDto> =
        ResponseEntity.ok(service.getByCode(code))

    /**
     * Create a new attribute definition.
     */
    @PostMapping
    suspend fun create(@Valid @RequestBody dto: AttributeMasterDto): ResponseEntity<AttributeMasterDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto))

    /**
     * Update an existing attribute definition.
     */
    @PutMapping("/{code}")
    suspend fun update(
        @PathVariable code: String,
        @Valid @RequestBody dto: AttributeMasterDto
    ): ResponseEntity<AttributeMasterDto> =
        ResponseEntity.ok(service.update(code, dto))

    /**
     * Soft-delete: archive an attribute (sets status to ARCHIVED).
     */
    @DeleteMapping("/{code}")
    suspend fun delete(@PathVariable code: String): ResponseEntity<Void> {
        service.softDelete(code)
        return ResponseEntity.noContent().build()
    }
}
