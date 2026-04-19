package com.marcusprado02.commons.ports.messaging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant

class MessagingPortTest :
    FunSpec({
        test("MessageId generates unique values") {
            val id1 = MessageId.generate()
            val id2 = MessageId.generate()
            (id1 == id2) shouldBe false
        }

        test("TopicName rejects blank value") {
            val ex = runCatching { TopicName("") }.exceptionOrNull()
            (ex is IllegalArgumentException) shouldBe true
        }

        test("ConsumerGroup rejects blank value") {
            val ex = runCatching { ConsumerGroup("") }.exceptionOrNull()
            (ex is IllegalArgumentException) shouldBe true
        }

        test("ConsumerGroup accepts non-blank value") {
            ConsumerGroup("my-group").value shouldBe "my-group"
        }

        test("publish is called with correct envelope") {
            val publisher = mockk<MessagePublisherPort>(relaxed = true)
            val envelope =
                MessageEnvelope(
                    topic = TopicName("orders"),
                    body = "payload".toByteArray(),
                    headers =
                        MessageHeaders(
                            messageId = MessageId.generate(),
                            timestamp = Instant.now(),
                        ),
                )
            publisher.publish(envelope)
            coVerify(exactly = 1) { publisher.publish(envelope) }
        }

        test("receive returns null when no message") {
            runTest {
                val consumer = mockk<MessageConsumerPort>()
                coEvery { consumer.receive(any(), any()) } returns null
                val result = consumer.receive(TopicName("t"), ConsumerGroup("g"))
                result shouldBe null
            }
        }

        test("acknowledge is invoked on consumer") {
            runTest {
                val consumer = mockk<MessageConsumerPort>(relaxed = true)
                val id = MessageId.generate()
                consumer.acknowledge(id)
                coVerify(exactly = 1) { consumer.acknowledge(id) }
            }
        }

        test("poll returns messages up to maxCount") {
            runTest {
                val topic = TopicName("orders")
                val group = ConsumerGroup("grp")
                val envelope =
                    MessageEnvelope(
                        topic = topic,
                        body = "data".toByteArray(),
                        headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                    )
                val responses = ArrayDeque(listOf(envelope, envelope, null))
                val consumer =
                    object : MessageConsumerPort {
                        override suspend fun receive(
                            topic: TopicName,
                            group: ConsumerGroup,
                        ): MessageEnvelope<ByteArray>? = responses.removeFirstOrNull()

                        override suspend fun acknowledge(messageId: MessageId) = Unit

                        override suspend fun nack(messageId: MessageId) = Unit
                    }
                val results = consumer.poll(topic, group, maxCount = 5)
                results.size shouldBe 2
            }
        }

        test("poll stops early when receive returns null") {
            runTest {
                val topic = TopicName("events")
                val group = ConsumerGroup("g2")
                val consumer =
                    object : MessageConsumerPort {
                        override suspend fun receive(
                            topic: TopicName,
                            group: ConsumerGroup,
                        ): MessageEnvelope<ByteArray>? = null

                        override suspend fun acknowledge(messageId: MessageId) = Unit

                        override suspend fun nack(messageId: MessageId) = Unit
                    }
                val results = consumer.poll(topic, group, maxCount = 10)
                results shouldBe emptyList()
            }
        }

        test("withHeader adds key-value to envelope extra headers") {
            val envelope =
                MessageEnvelope(
                    topic = TopicName("t"),
                    body = "x".toByteArray(),
                    headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                )
            val updated = envelope.withHeader("trace-id", "abc-123")
            updated.headers.extra["trace-id"] shouldBe "abc-123"
        }

        test("withHeader preserves existing headers") {
            val envelope =
                MessageEnvelope(
                    topic = TopicName("t"),
                    body = "x".toByteArray(),
                    headers =
                        MessageHeaders(
                            messageId = MessageId.generate(),
                            timestamp = Instant.now(),
                            extra = mapOf("existing" to "value"),
                        ),
                )
            val updated = envelope.withHeader("new-key", "new-val")
            updated.headers.extra["existing"] shouldBe "value"
            updated.headers.extra["new-key"] shouldBe "new-val"
        }

        test("DeadLetterPort send is called with correct arguments") {
            runTest {
                val dlq = mockk<DeadLetterPort>(relaxed = true)
                val topic = TopicName("payments")
                val envelope =
                    MessageEnvelope(
                        topic = topic,
                        body = "failed".toByteArray(),
                        headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                    )
                dlq.send(envelope, "processing error", topic)
                coVerify(exactly = 1) { dlq.send(envelope, "processing error", topic) }
            }
        }
    })
