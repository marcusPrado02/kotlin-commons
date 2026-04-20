package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

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
