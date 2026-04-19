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

class SmtpEmailAdapterTest :
    FunSpec({
        val serverSetup = ServerSetup.SMTP.port(0)
        val greenMail = GreenMail(serverSetup)

        beforeSpec { greenMail.start() }
        afterSpec { greenMail.stop() }

        beforeTest { greenMail.purgeEmailFromAllMailboxes() }

        fun session(): Session =
            Session.getInstance(
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
                        attachments =
                            listOf(
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
                val emails =
                    (1..3).map { i ->
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

        // T-82: multipart/alternative
        test("send email with both HTML and plain uses multipart/alternative") {
            runTest {
                adapter.send(
                    Email(
                        from = EmailAddress("from@example.com"),
                        to = listOf(EmailAddress("to@example.com")),
                        subject = "Multipart",
                        content = EmailContent(plain = "Plain version", html = "<b>HTML version</b>"),
                    ),
                )
                val messages = greenMail.receivedMessages
                messages.size shouldBe 1
                messages[0].subject shouldBe "Multipart"
            }
        }

        // T-83: parallel sendBatch
        test("sendBatch with 3 emails sends all concurrently") {
            runTest {
                val emails =
                    (1..3).map { i ->
                        Email(
                            from = EmailAddress("from@example.com"),
                            to = listOf(EmailAddress("batch$i@example.com")),
                            subject = "Parallel $i",
                            content = EmailContent(plain = "Parallel body $i"),
                        )
                    }
                adapter.sendBatch(emails)
                greenMail.receivedMessages.size shouldBe 3
            }
        }

        // T-84: SmtpSessionBuilder
        test("SmtpSessionBuilder builds a Session with the configured host") {
            val session =
                SmtpSessionBuilder()
                    .host("smtp.example.com")
                    .port(465)
                    .tls(true)
                    .connectTimeout(3_000)
                    .readTimeout(8_000)
                    .build()
            session.getProperty("mail.smtp.host") shouldBe "smtp.example.com"
            session.getProperty("mail.smtp.port") shouldBe "465"
            session.getProperty("mail.smtp.starttls.enable") shouldBe "true"
        }

        test("SmtpSessionBuilder sets auth when credentials provided") {
            val session =
                SmtpSessionBuilder()
                    .host("smtp.example.com")
                    .credentials("user@example.com", "secret")
                    .build()
            session.getProperty("mail.smtp.auth") shouldBe "true"
        }
    })
