package com.marcusprado02.commons.kernel.ddd.identity

import java.util.UUID

/**
 * Identifier that correlates a chain of requests, commands, and events to a single originating operation.
 *
 * @property value the non-blank string representing the correlation identifier.
 */
@JvmInline
public value class CorrelationId(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "CorrelationId must not be blank" }
    }

    override fun toString(): String = value

    public companion object {
        /** Generates a new random [CorrelationId] backed by a UUID. */
        public fun generate(): CorrelationId = CorrelationId(UUID.randomUUID().toString())
    }
}
