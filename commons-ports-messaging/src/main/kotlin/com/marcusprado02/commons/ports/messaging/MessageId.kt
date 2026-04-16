package com.marcusprado02.commons.ports.messaging

import java.util.UUID

@JvmInline
public value class MessageId(public val value: String) {
    override fun toString(): String = value
    public companion object {
        public fun generate(): MessageId = MessageId(UUID.randomUUID().toString())
    }
}
