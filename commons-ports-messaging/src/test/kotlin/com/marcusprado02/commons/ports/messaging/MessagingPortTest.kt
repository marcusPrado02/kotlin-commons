package com.marcusprado02.commons.ports.messaging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant

class MessagingPortTest : FunSpec({
    test("MessageId generates unique values") {
        val id1 = MessageId.generate()
        val id2 = MessageId.generate()
        (id1 == id2) shouldBe false
    }

    test("TopicName rejects blank value") {
        val ex = runCatching { TopicName("") }.exceptionOrNull()
        (ex is IllegalArgumentException) shouldBe true
    }

    test("publish is called with correct envelope") {
        val publisher = mockk<MessagePublisherPort>(relaxed = true)
        val envelope = MessageEnvelope(
            topic = TopicName("orders"),
            body = "payload".toByteArray(),
            headers = MessageHeaders(
                messageId = MessageId.generate(),
                timestamp = Instant.now(),
            ),
        )
        publisher.publish(envelope)
        coVerify(exactly = 1) { publisher.publish(envelope) }
    }
})
