package com.marcusprado02.commons.kernel.ddd.event

import java.util.UUID

@JvmInline
public value class EventId(public val value: String) {
    override fun toString(): String = value
    public companion object {
        public fun generate(): EventId = EventId(UUID.randomUUID().toString())
    }
}
