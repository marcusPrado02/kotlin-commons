package com.marcusprado02.commons.ports.messaging

@JvmInline
public value class TopicName(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "TopicName must not be blank" }
    }

    override fun toString(): String = value
}
