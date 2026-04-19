package com.marcusprado02.commons.adapters.email.smtp

import com.marcusprado02.commons.ports.email.EmailAddress
import com.marcusprado02.commons.ports.email.EmailAttachment
import com.marcusprado02.commons.ports.email.EmailContent
import jakarta.activation.DataHandler
import jakarta.mail.Part
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.util.ByteArrayDataSource

internal fun EmailAddress.toInternetAddress(): InternetAddress = InternetAddress(address, displayName, "UTF-8")

internal fun Part.applyContent(content: EmailContent) {
    when {
        content.html != null && content.plain != null -> {
            val multipart = MimeMultipart("alternative")
            val plainPart = MimeBodyPart().also { it.setContent(content.plain, "text/plain; charset=utf-8") }
            val htmlPart = MimeBodyPart().also { it.setContent(content.html, "text/html; charset=utf-8") }
            multipart.addBodyPart(plainPart)
            multipart.addBodyPart(htmlPart)
            setContent(multipart)
        }
        content.html != null -> setContent(content.html, "text/html; charset=utf-8")
        else -> setContent(content.plain ?: "", "text/plain; charset=utf-8")
    }
}

internal fun EmailAttachment.toMimeBodyPart(): MimeBodyPart =
    MimeBodyPart().also { part ->
        part.dataHandler = DataHandler(ByteArrayDataSource(content, mimeType))
        part.fileName = filename
    }
