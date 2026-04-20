package com.marcusprado02.commons.ports.email

/** Port for sending outgoing email messages. */
public interface EmailPort {
    /**
     * Sends a single [email].
     *
     * @param email the message to send.
     */
    public suspend fun send(email: Email)

    /**
     * Sends multiple emails, typically in a single provider call.
     *
     * @param emails the messages to send.
     */
    public suspend fun sendBatch(emails: List<Email>)
}
