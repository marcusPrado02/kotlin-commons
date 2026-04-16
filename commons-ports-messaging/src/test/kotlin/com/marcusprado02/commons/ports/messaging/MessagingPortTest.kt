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
    })
