package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.MessagePublisherPort
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public class KafkaMessagePublisherAdapter(
    private val producer: KafkaProducer<String, ByteArray>,
) : MessagePublisherPort {
    override suspend fun publish(envelope: MessageEnvelope<*>) {
        val body =
            envelope.body as? ByteArray
                ?: throw IllegalArgumentException(
                    "KafkaMessagePublisherAdapter requires ByteArray body, got ${envelope.body?.javaClass?.name}",
                )
        suspendCancellableCoroutine { cont ->
            val record =
                ProducerRecord(
                    envelope.topic.value,
                    envelope.headers.messageId.value,
                    body,
                )
            // Add correlation-id header if present
            envelope.headers.correlationId?.let {
                record.headers().add("correlation-id", it.toByteArray())
            }
            val future =
                producer.send(record) { _, ex ->
                    if (ex != null) cont.resumeWithException(ex) else cont.resume(Unit)
                }
            cont.invokeOnCancellation { future.cancel(true) }
        }
    }

    override suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>): Unit =
        coroutineScope { envelopes.forEach { launch { publish(it) } } }
}
