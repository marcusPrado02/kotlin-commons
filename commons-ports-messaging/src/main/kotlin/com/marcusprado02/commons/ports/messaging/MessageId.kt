package com.marcusprado02.commons.ports.messaging

import java.util.UUID

/** Unique identifier for a message, used for acknowledgement and deduplication. */
@JvmInline
public value class MessageId(
    public val value: String,
) {
    override fun toString(): String = value

    public companion object {
        /** Creates a new [MessageId] backed by a random UUID string. */
        public fun generate(): MessageId = MessageId(UUID.randomUUID().toString())
    }
}
