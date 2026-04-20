package com.marcusprado02.commons.kernel.ddd.identity

import java.util.UUID

/**
 * A UUID-based identity value object for use as aggregate or entity identifiers.
 *
 * @property value the underlying [UUID].
 */
@JvmInline
public value class UuidIdentifier(
    public val value: UUID,
) {
    override fun toString(): String = value.toString()

    public companion object {
        /** Generates a new random [UuidIdentifier]. */
        public fun generate(): UuidIdentifier = UuidIdentifier(UUID.randomUUID())

        /**
         * Parses a [UuidIdentifier] from a string.
         *
         * @param value a valid UUID string.
         * @throws IllegalArgumentException if [value] is not a valid UUID.
         */
        public fun of(value: String): UuidIdentifier = UuidIdentifier(UUID.fromString(value))
    }
}
