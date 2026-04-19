package com.marcusprado02.commons.adapters.email.smtp

import com.marcusprado02.commons.ports.email.Email
import com.marcusprado02.commons.ports.email.EmailPort
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class SmtpEmailAdapter(
    private val session: Session,
) : EmailPort {
    override suspend fun send(email: Email): Unit =
        withContext(Dispatchers.IO) {
            val message = MimeMessage(session)
            message.setFrom(email.from.toInternetAddress())
            email.to.forEach { message.addRecipient(Message.RecipientType.TO, it.toInternetAddress()) }
            email.cc.forEach { message.addRecipient(Message.RecipientType.CC, it.toInternetAddress()) }
            email.bcc.forEach { message.addRecipient(Message.RecipientType.BCC, it.toInternetAddress()) }
            email.replyTo?.let { message.replyTo = arrayOf(it.toInternetAddress()) }
            message.subject = email.subject

            if (email.attachments.isEmpty()) {
                message.applyContent(email.content)
            } else {
                val multipart = MimeMultipart()
                multipart.addBodyPart(MimeBodyPart().also { it.applyContent(email.content) })
                email.attachments.forEach { multipart.addBodyPart(it.toMimeBodyPart()) }
                message.setContent(multipart)
            }

            Transport.send(message)
        }

    override suspend fun sendBatch(emails: List<Email>): Unit = emails.forEach { send(it) }
}
