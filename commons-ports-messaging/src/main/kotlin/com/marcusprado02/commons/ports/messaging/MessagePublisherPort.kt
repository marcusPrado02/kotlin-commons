package com.marcusprado02.commons.ports.messaging

/** Port for publishing messages to a messaging broker. */
public interface MessagePublisherPort {
    /**
     * Publishes a single [envelope].
     *
     * @param envelope the message to publish.
     */
    public suspend fun publish(envelope: MessageEnvelope<*>)

    /**
     * Publishes multiple envelopes, typically in a single broker call.
     *
     * @param envelopes the messages to publish.
     */
    public suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>)
}
