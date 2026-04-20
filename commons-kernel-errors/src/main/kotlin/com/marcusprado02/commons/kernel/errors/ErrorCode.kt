package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

/**
 * Unique, machine-readable identifier for an error condition.
 * Must be a non-blank string (e.g. `"USER_NOT_FOUND"`).
 */
@Serializable
@JvmInline
public value class ErrorCode(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "ErrorCode must not be blank" }
    }

    override fun toString(): String = value
}
