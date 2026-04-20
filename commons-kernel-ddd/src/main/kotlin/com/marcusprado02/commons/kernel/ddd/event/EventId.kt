package com.marcusprado02.commons.kernel.ddd.event

import java.util.UUID

/**
 * Unique identifier for a [DomainEvent].
 *
 * @property value the underlying string representation, typically a UUID.
 */
@JvmInline
public value class EventId(
    public val value: String,
) {
    override fun toString(): String = value

    public companion object {
        /** Generates a new random [EventId] backed by a UUID. */
        public fun generate(): EventId = EventId(UUID.randomUUID().toString())
    }
}
