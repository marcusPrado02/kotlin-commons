package com.marcusprado02.commons.ports.messaging

@JvmInline
public value class ConsumerGroup(public val value: String)

public interface MessageConsumerPort {
    public suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>?
    public suspend fun acknowledge(messageId: MessageId)
    public suspend fun nack(messageId: MessageId)
}
