package com.marcusprado02.commons.ports.messaging

public data class MessageEnvelope<T>(
    val topic: TopicName,
    val body: T,
    val headers: MessageHeaders,
)

public fun <T> MessageEnvelope<T>.withHeader(
    key: String,
    value: String,
): MessageEnvelope<T> = copy(headers = headers.copy(extra = headers.extra + (key to value)))
