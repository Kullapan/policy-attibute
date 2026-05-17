package com.example.kk.policyattribute.model

/**
 * Lifecycle status for attribute definitions.
 * Soft-delete uses ARCHIVED instead of physical deletion.
 */
enum class AttributeStatus {
    ACTIVE,
    ARCHIVED
}
