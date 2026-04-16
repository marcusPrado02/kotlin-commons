package com.marcusprado02.commons.ports.messaging

public data class MessageEnvelope<T>(
    val topic: TopicName,
    val body: T,
    val headers: MessageHeaders,
)
