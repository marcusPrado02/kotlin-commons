package com.marcusprado02.commons.adapters.email.smtp

import com.marcusprado02.commons.ports.email.EmailAttachment
import com.marcusprado02.commons.ports.email.EmailContent
import jakarta.activation.DataHandler
import jakarta.mail.Part
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.util.ByteArrayDataSource

internal fun Part.applyContent(content: EmailContent) {
    when {
        content.html != null -> setContent(content.html, "text/html; charset=utf-8")
        else -> setContent(content.plain ?: "", "text/plain; charset=utf-8")
    }
}

internal fun EmailAttachment.toMimeBodyPart(): MimeBodyPart =
    MimeBodyPart().also { part ->
        part.dataHandler = DataHandler(ByteArrayDataSource(content, mimeType))
        part.fileName = filename
    }
