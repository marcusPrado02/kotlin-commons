package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.ConsumerGroup
import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.MessageHeaders
import com.marcusprado02.commons.ports.messaging.MessageId
import com.marcusprado02.commons.ports.messaging.MessageConsumerPort
import com.marcusprado02.commons.ports.messaging.TopicName
import kotlinx.coroutines.Dispatchers
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
) : MessageConsumerPort {

    private val kafkaDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val pending = ConcurrentHashMap<String, TopicPartitionOffset>()
    private val buffer = ConcurrentLinkedQueue<ConsumerRecord<String, ByteArray>>()
    private var subscribedTopics: Set<String> = emptySet()

    override suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>? =
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
                return@withContext MessageEnvelope(
                    topic = TopicName(r.topic()),
                    body = r.value(),
                    headers = MessageHeaders(messageId = id, timestamp = Instant.ofEpochMilli(r.timestamp())),
                )
            }
            // Poll Kafka
            consumer.poll(Duration.ofMillis(500)).forEach { buffer.offer(it) }
            // Return matching record from freshly-filled buffer
            buffer.firstOrNull { it.topic() == topic.value }?.also { buffer.remove(it) }?.let { r ->
                val id = MessageId(r.key() ?: UUID.randomUUID().toString())
                pending[id.value] = TopicPartitionOffset(r.topic(), r.partition(), r.offset())
                MessageEnvelope(
                    topic = TopicName(r.topic()),
                    body = r.value(),
                    headers = MessageHeaders(messageId = id, timestamp = Instant.ofEpochMilli(r.timestamp())),
                )
            }
        }

    override suspend fun acknowledge(messageId: MessageId): Unit =
        withContext(kafkaDispatcher) {
            pending.remove(messageId.value)?.let { tpo ->
                consumer.commitSync(tpo.toOffsetMap())
            }
        }

    override suspend fun nack(messageId: MessageId): Unit =
        withContext(kafkaDispatcher) {
            pending.remove(messageId.value)?.let { tpo ->
                consumer.seek(
                    TopicPartition(tpo.topic, tpo.partition),
                    tpo.offset,
                )
            }
        }
}
