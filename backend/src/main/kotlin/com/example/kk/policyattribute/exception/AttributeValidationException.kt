package com.example.kk.policyattribute.exception

/**
 * Thrown when an attribute value fails validation against its
 * data_type or regex_pattern.
 */
class AttributeValidationException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(attributeCode: String, detail: String) :
        super("Validation failed for attribute [$attributeCode]: $detail")
}
