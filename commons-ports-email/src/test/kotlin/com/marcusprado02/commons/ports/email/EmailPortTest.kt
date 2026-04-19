package com.marcusprado02.commons.ports.email

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk

class EmailPortTest :
    FunSpec({
        test("EmailAddress validates @ symbol") {
            val ea = EmailAddress("user@example.com", "User")
            ea.address shouldBe "user@example.com"
            ea.displayName shouldBe "User"
        }

        test("EmailAddress rejects invalid address") {
            shouldThrow<IllegalArgumentException> { EmailAddress("not-an-email") }
        }

        test("EmailContent requires at least html or plain") {
            shouldThrow<IllegalArgumentException> { EmailContent(html = null, plain = null) }
        }

        test("Email requires at least one recipient") {
            shouldThrow<IllegalArgumentException> {
                Email(
                    from = EmailAddress("from@example.com"),
                    to = emptyList(),
                    subject = "Test",
                    content = EmailContent(plain = "body"),
                )
            }
        }

        test("EmailPort.send is called with correct email") {
            val port = mockk<EmailPort>(relaxed = true)
            val email =
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "Hello",
                    content = EmailContent(plain = "Hi there"),
                )
            port.send(email)
            coVerify(exactly = 1) { port.send(email) }
        }

        test("EmailAddress toString with displayName") {
            val ea = EmailAddress("user@example.com", "Display Name")
            ea.toString() shouldBe "Display Name <user@example.com>"
        }

        test("EmailAddress toString without displayName") {
            val ea = EmailAddress("user@example.com")
            ea.toString() shouldBe "user@example.com"
        }

        test("EmailContent with html only") {
            val content = EmailContent(html = "<p>Hello</p>")
            content.html shouldBe "<p>Hello</p>"
            content.plain shouldBe null
        }

        test("Email rejects blank subject") {
            shouldThrow<IllegalArgumentException> {
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "  ",
                    content = EmailContent(plain = "body"),
                )
            }
        }

        test("EmailAttachment equality") {
            val bytes = byteArrayOf(1, 2, 3)
            val a1 = EmailAttachment("file.txt", bytes, "text/plain")
            val a2 = EmailAttachment("file.txt", bytes, "text/plain")
            (a1 == a2) shouldBe true
            a1.hashCode() shouldBe a2.hashCode()
        }

        test("EmailAttachment inequality") {
            val a1 = EmailAttachment("file.txt", byteArrayOf(1), "text/plain")
            val a2 = EmailAttachment("other.txt", byteArrayOf(2), "text/plain")
            (a1 == a2) shouldBe false
        }

        test("EmailContent.withBoth creates content with both html and plain") {
            val content = EmailContent.withBoth(plain = "Hello", html = "<p>Hello</p>")
            content.plain shouldBe "Hello"
            content.html shouldBe "<p>Hello</p>"
        }

        test("EmailContent.withBoth satisfies init constraint") {
            val content = EmailContent.withBoth(plain = "text", html = "<b>text</b>")
            (content.html != null || content.plain != null) shouldBe true
        }

        test("Email.withHeader adds header to email") {
            val email =
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "Test",
                    content = EmailContent(plain = "body"),
                )
            val updated = email.withHeader("X-Custom-Header", "value123")
            updated.headers["X-Custom-Header"] shouldBe "value123"
        }

        test("Email.withHeader preserves existing headers") {
            val email =
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "Test",
                    content = EmailContent(plain = "body"),
                    headers = mapOf("X-Existing" to "exists"),
                )
            val updated = email.withHeader("X-New", "new-val")
            updated.headers["X-Existing"] shouldBe "exists"
            updated.headers["X-New"] shouldBe "new-val"
        }
    })
