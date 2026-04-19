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
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

public class KafkaMessageConsumerAdapter(
    private val consumer: KafkaConsumer<String, ByteArray>,
    private val groupId: String,
) : MessageConsumerPort {

    private val pending = ConcurrentHashMap<String, TopicPartitionOffset>()
    private val buffer = ConcurrentLinkedQueue<ConsumerRecord<String, ByteArray>>()

    override suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>? =
        withContext(Dispatchers.IO) {
            require(group.value == groupId) {
                "Consumer group mismatch: adapter configured for '$groupId', received '${group.value}'"
            }
            consumer.subscribe(listOf(topic.value))
            val record = buffer.poll() ?: run {
                val records = consumer.poll(Duration.ofMillis(500))
                val first = records.firstOrNull()
                records.drop(1).forEach { buffer.add(it) }
                first
            }
            record?.let { r ->
                val id = MessageId(r.key() ?: UUID.randomUUID().toString())
                pending[id.value] = TopicPartitionOffset(r.topic(), r.partition(), r.offset())
                MessageEnvelope(
                    topic = topic,
                    body = r.value(),
                    headers = MessageHeaders(
                        messageId = id,
                        timestamp = Instant.ofEpochMilli(r.timestamp()),
                    ),
                )
            }
        }

    override suspend fun acknowledge(messageId: MessageId): Unit =
        withContext(Dispatchers.IO) {
            pending.remove(messageId.value)?.let { tpo ->
                consumer.commitSync(tpo.toOffsetMap())
            }
        }

    override suspend fun nack(messageId: MessageId) {
        pending.remove(messageId.value)
        // offset not committed — Kafka redelivers on next consumer session (at-least-once)
    }
}
