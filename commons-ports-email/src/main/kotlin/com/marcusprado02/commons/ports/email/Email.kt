package com.marcusprado02.commons.ports.email

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

    public fun withHeader(
        name: String,
        value: String,
    ): Email = copy(headers = headers + (name to value))
}
