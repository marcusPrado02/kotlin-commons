package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.ConsumerGroup
import com.marcusprado02.commons.ports.messaging.DeadLetterPort
import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.MessageHeaders
import com.marcusprado02.commons.ports.messaging.MessageId
import com.marcusprado02.commons.ports.messaging.TopicName
import com.marcusprado02.commons.testkit.testcontainers.KafkaContainers
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

private const val RECEIVE_RETRY_ATTEMPTS = 20
private const val RECEIVE_RETRY_DELAY_MS = 300L

/**
 * Retries [KafkaMessageConsumerAdapter.receive] until a message arrives or [RECEIVE_RETRY_ATTEMPTS]
 * is exhausted. Necessary on CI because the first poll triggers consumer group rebalancing, which
 * may take longer than the single poll timeout.
 */
private suspend fun KafkaMessageConsumerAdapter.receiveWithRetry(
    topic: TopicName,
    group: ConsumerGroup,
): MessageEnvelope<ByteArray>? {
    repeat(RECEIVE_RETRY_ATTEMPTS) {
        val msg = receive(topic, group)
        if (msg != null) return msg
        delay(RECEIVE_RETRY_DELAY_MS)
    }
    return null
}

class KafkaAdapterTest :
    FunSpec({
        val bootstrap = KafkaContainers.instance.bootstrapServers
        val topic = TopicName("test-topic")
        val group = ConsumerGroup("test-group")

        val producer =
            KafkaProducer<String, ByteArray>(
                mapOf(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java.name,
                ),
            )
        val consumer =
            KafkaConsumer<String, ByteArray>(
                mapOf(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                    ConsumerConfig.GROUP_ID_CONFIG to group.value,
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
                ),
            )

        val publisher = KafkaMessagePublisherAdapter(producer)
        val consumerAdapter = KafkaMessageConsumerAdapter(consumer, group.value)

        afterSpec {
            producer.close()
            consumer.close()
        }

        test("publish sends a message that can be received") {
            runTest(timeout = 30.seconds) {
                val envelope =
                    MessageEnvelope(
                        topic = topic,
                        body = "hello-kafka".toByteArray(),
                        headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                    )
                publisher.publish(envelope)
                val received = consumerAdapter.receiveWithRetry(topic, group)
                received shouldNotBe null
                String(received!!.body) shouldBe "hello-kafka"
            }
        }

        test("acknowledge commits offset without error") {
            runTest(timeout = 30.seconds) {
                val envelope =
                    MessageEnvelope(
                        topic = topic,
                        body = "ack-test".toByteArray(),
                        headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                    )
                publisher.publish(envelope)
                val received = consumerAdapter.receiveWithRetry(topic, group)
                received shouldNotBe null
                consumerAdapter.acknowledge(received!!.headers.messageId)
            }
        }

        test("publishBatch sends all messages") {
            runTest(timeout = 30.seconds) {
                val envelopes =
                    (1..3).map { i ->
                        MessageEnvelope(
                            topic = topic,
                            body = "batch-$i".toByteArray(),
                            headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                        )
                    }
                publisher.publishBatch(envelopes)
                var count = 0
                repeat(3) {
                    val msg = consumerAdapter.receiveWithRetry(topic, group)
                    if (msg != null) count++
                }
                count shouldBe 3
            }
        }

        test("publish throws IllegalArgumentException for non-ByteArray body") {
            runTest {
                val envelope =
                    MessageEnvelope(
                        topic = topic,
                        body = "not-bytes",
                        headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                    )
                shouldThrow<IllegalArgumentException> {
                    publisher.publish(envelope)
                }
            }
        }

        test("nack removes message from pending without error") {
            runTest(timeout = 30.seconds) {
                val envelope =
                    MessageEnvelope(
                        topic = topic,
                        body = "nack-test".toByteArray(),
                        headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                    )
                publisher.publish(envelope)
                val received = consumerAdapter.receiveWithRetry(topic, group)
                received shouldNotBe null
                consumerAdapter.nack(received!!.headers.messageId)
            }
        }

        test("acknowledge with unknown messageId does nothing") {
            runTest {
                consumerAdapter.acknowledge(MessageId.generate())
            }
        }

        test("receive returns null when no messages available") {
            runTest {
                val emptyTopic = TopicName("empty-topic-${System.currentTimeMillis()}")
                val received = consumerAdapter.receive(emptyTopic, group)
                received shouldBe null
            }
        }

        test("correlationId is propagated through publish and receive") {
            runTest(timeout = 30.seconds) {
                val corrGroupId = "correlation-group-${System.currentTimeMillis()}"
                val correlationTopic = TopicName("correlation-topic-${System.currentTimeMillis()}")
                val envelope =
                    MessageEnvelope(
                        topic = correlationTopic,
                        body = "corr-test".toByteArray(),
                        headers =
                            MessageHeaders(
                                messageId = MessageId.generate(),
                                timestamp = Instant.now(),
                                correlationId = "test-correlation-123",
                            ),
                    )
                publisher.publish(envelope)

                val correlationConsumer =
                    KafkaConsumer<String, ByteArray>(
                        mapOf(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                            ConsumerConfig.GROUP_ID_CONFIG to corrGroupId,
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
                        ),
                    )
                val correlationConsumerAdapter = KafkaMessageConsumerAdapter(correlationConsumer, corrGroupId)
                try {
                    val received =
                        correlationConsumerAdapter.receiveWithRetry(
                            correlationTopic,
                            ConsumerGroup(corrGroupId),
                        )
                    received shouldNotBe null
                    received!!.headers.correlationId shouldBe "test-correlation-123"
                } finally {
                    correlationConsumer.close()
                }
            }
        }

        test("close() can be called without error") {
            runTest {
                val closeConsumer =
                    KafkaConsumer<String, ByteArray>(
                        mapOf(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                            ConsumerConfig.GROUP_ID_CONFIG to "close-group-${System.currentTimeMillis()}",
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
                        ),
                    )
                val closeConsumerAdapter = KafkaMessageConsumerAdapter(closeConsumer, "close-group")
                closeConsumerAdapter.close()
            }
        }

        test("dead-letter port is invoked after maxNacks nacks") {
            runTest(timeout = 30.seconds) {
                val dlTopic = TopicName("dl-test-topic-${System.currentTimeMillis()}")
                val dlGroup = ConsumerGroup("dl-group-${System.currentTimeMillis()}")
                val dlDeadLetterPort = mockk<DeadLetterPort>(relaxed = true)
                val dlConsumer =
                    KafkaConsumer<String, ByteArray>(
                        mapOf(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                            ConsumerConfig.GROUP_ID_CONFIG to dlGroup.value,
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
                        ),
                    )
                val dlAdapter =
                    KafkaMessageConsumerAdapter(
                        consumer = dlConsumer,
                        groupId = dlGroup.value,
                        maxNacks = 3,
                        deadLetterPort = dlDeadLetterPort,
                    )
                try {
                    val envelope =
                        MessageEnvelope(
                            topic = dlTopic,
                            body = "dl-body".toByteArray(),
                            headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                        )
                    publisher.publish(envelope)
                    val received = dlAdapter.receiveWithRetry(dlTopic, dlGroup)
                    received shouldNotBe null
                    val msgId = received!!.headers.messageId
                    dlAdapter.nack(msgId)
                    dlAdapter.nack(msgId)
                    dlAdapter.nack(msgId)
                    coVerify(exactly = 1) { dlDeadLetterPort.send(any(), any(), any()) }
                } finally {
                    dlConsumer.close()
                }
            }
        }

        test("dead-letter port is not invoked below maxNacks") {
            runTest(timeout = 30.seconds) {
                val dlTopic2 = TopicName("dl-below-topic-${System.currentTimeMillis()}")
                val dlGroup2 = ConsumerGroup("dl-below-group-${System.currentTimeMillis()}")
                val dlDeadLetterPort2 = mockk<DeadLetterPort>(relaxed = true)
                val dlConsumer2 =
                    KafkaConsumer<String, ByteArray>(
                        mapOf(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
                            ConsumerConfig.GROUP_ID_CONFIG to dlGroup2.value,
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
                        ),
                    )
                val dlAdapter2 =
                    KafkaMessageConsumerAdapter(
                        consumer = dlConsumer2,
                        groupId = dlGroup2.value,
                        maxNacks = 3,
                        deadLetterPort = dlDeadLetterPort2,
                    )
                try {
                    val envelope =
                        MessageEnvelope(
                            topic = dlTopic2,
                            body = "dl-below-body".toByteArray(),
                            headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                        )
                    publisher.publish(envelope)
                    val received = dlAdapter2.receiveWithRetry(dlTopic2, dlGroup2)
                    received shouldNotBe null
                    val msgId = received!!.headers.messageId
                    dlAdapter2.nack(msgId)
                    dlAdapter2.nack(msgId)
                    coVerify(exactly = 0) { dlDeadLetterPort2.send(any(), any(), any()) }
                } finally {
                    dlConsumer2.close()
                }
            }
        }
    })
