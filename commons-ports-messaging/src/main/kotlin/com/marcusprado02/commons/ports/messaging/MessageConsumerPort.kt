package com.marcusprado02.commons.ports.messaging

@JvmInline
public value class ConsumerGroup(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "ConsumerGroup must not be blank" }
    }
}

public interface MessageConsumerPort {
    public suspend fun receive(
        topic: TopicName,
        group: ConsumerGroup,
    ): MessageEnvelope<ByteArray>?

    public suspend fun acknowledge(messageId: MessageId)

    public suspend fun nack(messageId: MessageId)

    public suspend fun poll(
        topic: TopicName,
        group: ConsumerGroup,
        maxCount: Int,
    ): List<MessageEnvelope<ByteArray>> {
        val results = mutableListOf<MessageEnvelope<ByteArray>>()
        repeat(maxCount) {
            val msg = receive(topic, group) ?: return results
            results += msg
        }
        return results
    }
}
