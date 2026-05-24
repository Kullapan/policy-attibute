package com.example.kk.policyattribute.controller

import com.example.kk.policyattribute.dto.AttributeGroupDto
import com.example.kk.policyattribute.service.AttributeGroupService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for Attribute Group lookup.
 */
@RestController
@RequestMapping("/api/v1/attribute-groups")
class AttributeGroupController(
    private val service: AttributeGroupService
) {

    /**
     * Get attribute groups, optionally including archived groups.
     */
    @GetMapping
    fun listGroups(
        @RequestParam(name = "includeArchived", required = false, defaultValue = "false") includeArchived: Boolean
    ): ResponseEntity<List<AttributeGroupDto>> =
        ResponseEntity.ok(service.listGroups(includeArchived))

    /**
     * Get a single group by code.
     */
    @GetMapping("/{code}")
    fun getByCode(@PathVariable code: String): ResponseEntity<AttributeGroupDto> =
        ResponseEntity.ok(service.getByCode(code))

    /**
     * Create a new attribute group.
     */
    @PostMapping
    fun create(@Valid @RequestBody dto: AttributeGroupDto): ResponseEntity<AttributeGroupDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto))

    /**
     * Update an existing attribute group.
     */
    @PutMapping("/{code}")
    fun update(
        @PathVariable code: String,
        @Valid @RequestBody dto: AttributeGroupDto
    ): ResponseEntity<AttributeGroupDto> =
        ResponseEntity.ok(service.update(code, dto))

    /**
     * Soft-delete: archive an attribute group (sets status to ARCHIVED).
     */
    @DeleteMapping("/{code}")
    fun delete(@PathVariable code: String): ResponseEntity<Void> {
        service.softDelete(code)
        return ResponseEntity.noContent().build()
    }
}

