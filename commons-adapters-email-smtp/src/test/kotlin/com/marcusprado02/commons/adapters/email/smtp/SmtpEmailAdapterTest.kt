package com.marcusprado02.commons.adapters.email.smtp

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.marcusprado02.commons.ports.email.Email
import com.marcusprado02.commons.ports.email.EmailAddress
import com.marcusprado02.commons.ports.email.EmailAttachment
import com.marcusprado02.commons.ports.email.EmailContent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import jakarta.mail.Session
import kotlinx.coroutines.test.runTest
import java.util.Properties

class SmtpEmailAdapterTest : FunSpec({
    val serverSetup = ServerSetup.SMTP.port(0)
    val greenMail = GreenMail(serverSetup)

    beforeSpec { greenMail.start() }
    afterSpec { greenMail.stop() }

    beforeTest { greenMail.purgeEmailFromAllMailboxes() }

    fun session(): Session = Session.getInstance(
        Properties().apply {
            put("mail.smtp.host", "127.0.0.1")
            put("mail.smtp.port", greenMail.smtp.port)
            put("mail.smtp.auth", "false")
        },
    )

    val adapter by lazy { SmtpEmailAdapter(session()) }

    test("send plain text email") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "Hello",
                    content = EmailContent(plain = "Plain body"),
                ),
            )
            val messages = greenMail.receivedMessages
            messages.size shouldBe 1
            messages[0].subject shouldBe "Hello"
        }
    }

    test("send HTML email") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "HTML",
                    content = EmailContent(html = "<b>Bold</b>"),
                ),
            )
            greenMail.receivedMessages.size shouldBe 1
        }
    }

    test("send email with attachment") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "With attachment",
                    content = EmailContent(plain = "See attached"),
                    attachments = listOf(
                        EmailAttachment(
                            filename = "file.txt",
                            content = "data".toByteArray(),
                            mimeType = "text/plain",
                        ),
                    ),
                ),
            )
            greenMail.receivedMessages.size shouldBe 1
        }
    }

    test("sendBatch sends all emails") {
        runTest {
            val emails = (1..3).map { i ->
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to$i@example.com")),
                    subject = "Batch $i",
                    content = EmailContent(plain = "Body $i"),
                )
            }
            adapter.sendBatch(emails)
            greenMail.receivedMessages.size shouldBe 3
        }
    }

    test("send to multiple recipients and cc") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("a@example.com"), EmailAddress("b@example.com")),
                    subject = "Multi",
                    content = EmailContent(plain = "hi"),
                    cc = listOf(EmailAddress("c@example.com")),
                ),
            )
            greenMail.receivedMessages.size shouldBe 3
        }
    }
})
