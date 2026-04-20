package com.marcusprado02.commons.ports.messaging

/** Identifies a named group of consumers that collectively process messages from a topic. */
@JvmInline
public value class ConsumerGroup(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "ConsumerGroup must not be blank" }
    }
}

/** Port for receiving and acknowledging messages from a messaging broker. */
public interface MessageConsumerPort {
    /**
     * Attempts to receive the next available message from [topic] for [group].
     *
     * @param topic the topic to consume from.
     * @param group the consumer group.
     * @return the next message envelope, or `null` if no message is available.
     */
    public suspend fun receive(
        topic: TopicName,
        group: ConsumerGroup,
    ): MessageEnvelope<ByteArray>?

    /**
     * Acknowledges successful processing of the message identified by [messageId].
     *
     * @param messageId the ID of the message to acknowledge.
     */
    public suspend fun acknowledge(messageId: MessageId)

    /**
     * Signals that processing of the message identified by [messageId] failed
     * and it should be re-delivered or sent to a dead-letter queue.
     *
     * @param messageId the ID of the message to nack.
     */
    public suspend fun nack(messageId: MessageId)

    /**
     * Polls up to [maxCount] messages from [topic] for [group].
     * Stops early if no more messages are available.
     *
     * @param topic the topic to consume from.
     * @param group the consumer group.
     * @param maxCount maximum number of messages to retrieve.
     */
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
