package com.marcusprado02.commons.kernel.ddd.identity

import java.util.UUID

@JvmInline
public value class UuidIdentifier(
    public val value: UUID,
) {
    override fun toString(): String = value.toString()

    public companion object {
        public fun generate(): UuidIdentifier = UuidIdentifier(UUID.randomUUID())

        public fun of(value: String): UuidIdentifier = UuidIdentifier(UUID.fromString(value))
    }
}
