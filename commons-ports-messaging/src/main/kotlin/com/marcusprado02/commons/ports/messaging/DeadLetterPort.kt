package com.marcusprado02.commons.ports.messaging

/** Port for routing messages that have failed processing repeatedly. */
public interface DeadLetterPort {
    public suspend fun send(
        envelope: MessageEnvelope<ByteArray>,
        reason: String,
        originalTopic: TopicName,
    )
}
