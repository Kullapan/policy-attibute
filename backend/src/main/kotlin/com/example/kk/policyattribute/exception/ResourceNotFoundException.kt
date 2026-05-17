package com.example.kk.policyattribute.exception

/**
 * Thrown when a requested resource is not found.
 */
class ResourceNotFoundException(resourceName: String, identifier: String) :
    RuntimeException("$resourceName not found with identifier: $identifier")
