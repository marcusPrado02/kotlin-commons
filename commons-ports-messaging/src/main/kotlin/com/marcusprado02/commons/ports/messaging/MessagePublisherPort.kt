package com.marcusprado02.commons.ports.messaging

public interface MessagePublisherPort {
    public suspend fun publish(envelope: MessageEnvelope<*>)
    public suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>)
}
