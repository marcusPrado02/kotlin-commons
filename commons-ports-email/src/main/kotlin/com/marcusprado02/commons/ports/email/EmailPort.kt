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

    /**
     * Renders a template and sends the result to [to].
     *
     * The default implementation throws [UnsupportedOperationException]. Adapters that support
     * template rendering (e.g., Thymeleaf, Freemarker) should override this method.
     *
     * @param templateId identifier of the template to render (e.g., a classpath resource name).
     * @param context key-value pairs made available to the template engine.
     * @param to the recipient address.
     */
    public suspend fun sendTemplate(
        templateId: String,
        context: Map<String, Any>,
        to: EmailAddress,
    ): Unit = throw UnsupportedOperationException("Template emails are not supported by this adapter")
}
