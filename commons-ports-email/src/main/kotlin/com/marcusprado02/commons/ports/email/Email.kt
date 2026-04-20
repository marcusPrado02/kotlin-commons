package com.marcusprado02.commons.ports.email

/**
 * Complete representation of an outgoing email message.
 *
 * @property from sender address.
 * @property to primary recipients (must not be empty).
 * @property subject email subject (must not be blank).
 * @property content body of the email.
 * @property cc carbon-copy recipients.
 * @property bcc blind carbon-copy recipients.
 * @property replyTo optional reply-to address.
 * @property attachments file attachments.
 * @property headers additional custom SMTP headers.
 */
public data class Email(
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val subject: String,
    val content: EmailContent,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val replyTo: EmailAddress? = null,
    val attachments: List<EmailAttachment> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
) {
    init {
        require(to.isNotEmpty()) { "Email must have at least one recipient" }
        require(subject.isNotBlank()) { "Email subject must not be blank" }
    }

    /**
     * Returns a copy of this email with the custom header [name]=[value] added.
     *
     * @param name header name.
     * @param value header value.
     */
    public fun withHeader(
        name: String,
        value: String,
    ): Email = copy(headers = headers + (name to value))
}
