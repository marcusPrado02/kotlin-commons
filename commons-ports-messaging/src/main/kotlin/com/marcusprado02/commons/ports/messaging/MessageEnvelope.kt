package com.marcusprado02.commons.ports.messaging

/**
 * A message together with its routing and metadata context.
 *
 * @param T the type of the message body.
 * @property topic the topic this message is associated with.
 * @property body the message payload.
 * @property headers metadata carried with the message.
 */
public data class MessageEnvelope<T>(
    val topic: TopicName,
    val body: T,
    val headers: MessageHeaders,
)

/**
 * Returns a copy of this envelope with [key]=[value] added to [MessageHeaders.extra].
 *
 * @param key header key.
 * @param value header value.
 */
public fun <T> MessageEnvelope<T>.withHeader(
    key: String,
    value: String,
): MessageEnvelope<T> = copy(headers = headers.copy(extra = headers.extra + (key to value)))
