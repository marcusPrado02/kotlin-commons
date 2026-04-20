package com.marcusprado02.commons.ports.messaging

/** Typed, non-blank identifier for a messaging topic or queue. */
@JvmInline
public value class TopicName(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "TopicName must not be blank" }
    }

    override fun toString(): String = value
}
