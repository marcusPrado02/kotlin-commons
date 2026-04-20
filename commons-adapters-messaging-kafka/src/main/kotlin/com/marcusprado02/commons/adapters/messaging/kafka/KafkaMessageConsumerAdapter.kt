package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.ConsumerGroup
import com.marcusprado02.commons.ports.messaging.DeadLetterPort
import com.marcusprado02.commons.ports.messaging.MessageConsumerPort
import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.MessageHeaders
import com.marcusprado02.commons.ports.messaging.MessageId
import com.marcusprado02.commons.ports.messaging.TopicName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

public class KafkaMessageConsumerAdapter(
    private val consumer: KafkaConsumer<String, ByteArray>,
    private val groupId: String,
    private val maxNacks: Int = 3,
    private val deadLetterPort: DeadLetterPort? = null,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
) : MessageConsumerPort {
    private val kafkaDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val pollTimeout: Duration = Duration.ofMillis(POLL_TIMEOUT_MS)
    private val pending = ConcurrentHashMap<String, TopicPartitionOffset>()
    private val buffer = ConcurrentLinkedQueue<ConsumerRecord<String, ByteArray>>()
    private var subscribedTopics: Set<String> = emptySet()
    private val nackCounts = ConcurrentHashMap<String, Int>()
    private val pendingEnvelopes = ConcurrentHashMap<String, MessageEnvelope<ByteArray>>()

    override suspend fun receive(
        topic: TopicName,
        group: ConsumerGroup,
    ): MessageEnvelope<ByteArray>? =
        withContext(kafkaDispatcher) {
            require(group.value == groupId) {
                "Consumer group mismatch: adapter configured for '$groupId', received '${group.value}'"
            }
            val topicSet = setOf(topic.value)
            if (subscribedTopics != topicSet) {
                consumer.subscribe(topicSet)
                subscribedTopics = topicSet
            }
            // Try to return from buffer first (matching topic)
            buffer.firstOrNull { it.topic() == topic.value }?.also { buffer.remove(it) }?.let { r ->
                val id = MessageId(r.key() ?: UUID.randomUUID().toString())
                pending[id.value] = TopicPartitionOffset(r.topic(), r.partition(), r.offset())
                val correlationId =
                    r
                        .headers()
                        .lastHeader("correlation-id")
                        ?.value()
                        ?.let { String(it) }
                val envelope =
                    MessageEnvelope(
                        topic = TopicName(r.topic()),
                        body = r.value(),
                        headers =
                            MessageHeaders(
                                messageId = id,
                                timestamp = Instant.ofEpochMilli(r.timestamp()),
                                correlationId = correlationId,
                            ),
                    )
                pendingEnvelopes[id.value] = envelope
                return@withContext envelope
            }
            // Poll Kafka
            consumer.poll(pollTimeout).forEach { buffer.offer(it) }
            // Return matching record from freshly-filled buffer
            buffer.firstOrNull { it.topic() == topic.value }?.also { buffer.remove(it) }?.let { r ->
                val id = MessageId(r.key() ?: UUID.randomUUID().toString())
                pending[id.value] = TopicPartitionOffset(r.topic(), r.partition(), r.offset())
                val correlationId =
                    r
                        .headers()
                        .lastHeader("correlation-id")
                        ?.value()
                        ?.let { String(it) }
                val envelope =
                    MessageEnvelope(
                        topic = TopicName(r.topic()),
                        body = r.value(),
                        headers =
                            MessageHeaders(
                                messageId = id,
                                timestamp = Instant.ofEpochMilli(r.timestamp()),
                                correlationId = correlationId,
                            ),
                    )
                pendingEnvelopes[id.value] = envelope
                envelope
            }
        }

    override suspend fun acknowledge(messageId: MessageId): Unit =
        withContext(kafkaDispatcher) {
            pendingEnvelopes.remove(messageId.value)
            nackCounts.remove(messageId.value)
            pending.remove(messageId.value)?.let { tpo ->
                consumer.commitSync(tpo.toOffsetMap())
            }
        }

    override suspend fun nack(messageId: MessageId): Unit =
        withContext(kafkaDispatcher) {
            val count = nackCounts.merge(messageId.value, 1, Int::plus) ?: 1
            if (count >= maxNacks && deadLetterPort != null) {
                pendingEnvelopes.remove(messageId.value)?.let { envelope ->
                    val tpo = pending.remove(messageId.value)
                    deadLetterPort.send(envelope, "Nacked $count times", TopicName(tpo?.topic ?: "unknown"))
                }
                nackCounts.remove(messageId.value)
                return@withContext
            }
            val attempt = nackCounts.getOrDefault(messageId.value, 1) - 1
            val delayMs = retryPolicy.delayFor(attempt)
            if (delayMs > 0) delay(delayMs)
            pending.remove(messageId.value)?.let { tpo ->
                consumer.seek(
                    TopicPartition(tpo.topic, tpo.partition),
                    tpo.offset,
                )
            }
        }

    public suspend fun close(): Unit =
        withContext(kafkaDispatcher) {
            consumer.unsubscribe()
            consumer.close()
        }

    public companion object {
        private const val POLL_TIMEOUT_MS = 500L
    }
}
