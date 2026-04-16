package com.marcusprado02.commons.kernel.ddd.identity

import java.util.UUID

@JvmInline
public value class CorrelationId(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "CorrelationId must not be blank" }
    }

    override fun toString(): String = value

    public companion object {
        public fun generate(): CorrelationId = CorrelationId(UUID.randomUUID().toString())
    }
}
