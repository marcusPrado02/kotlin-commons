package com.marcusprado02.commons.ports.http

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.net.URI

class HttpPortTest :
    FunSpec({
        test("HttpResponse.isSuccessful for 200 range") {
            val r = HttpResponse<ByteArray>(200, emptyMap(), null)
            r.isSuccessful shouldBe true
            r.isClientError shouldBe false
            r.isServerError shouldBe false
        }

        test("HttpResponse.isClientError for 400 range") {
            val r = HttpResponse<ByteArray>(404, emptyMap(), null)
            r.isClientError shouldBe true
            r.isSuccessful shouldBe false
        }

        test("HttpClientPort.execute returns mocked response") {
            val client = mockk<HttpClientPort>()
            val request = HttpRequest(URI.create("https://example.com"), HttpMethod.GET)
            val response = HttpResponse(200, emptyMap(), "hello".toByteArray())

            coEvery { client.execute(request) } returns response

            val result = client.execute(request)
            result.statusCode shouldBe 200
        }

        test("HttpBody.Bytes equality uses content equality not reference") {
            val bytes = byteArrayOf(1, 2, 3)
            val a = HttpBody.Bytes(bytes.copyOf(), "application/octet-stream")
            val b = HttpBody.Bytes(bytes.copyOf(), "application/octet-stream")
            a shouldBe b
        }

        test("HttpBody.Bytes with different content are not equal") {
            val a = HttpBody.Bytes(byteArrayOf(1, 2, 3), "application/octet-stream")
            val b = HttpBody.Bytes(byteArrayOf(4, 5, 6), "application/octet-stream")
            (a == b) shouldBe false
        }

        test("MultipartPart equality uses content equality not reference") {
            val bytes = byteArrayOf(10, 20, 30)
            val a = MultipartPart("file", bytes.copyOf(), "image/png", "photo.png")
            val b = MultipartPart("file", bytes.copyOf(), "image/png", "photo.png")
            a shouldBe b
        }

        test("HttpBody.Bytes same reference is equal") {
            val a = HttpBody.Bytes(byteArrayOf(1, 2, 3), "text/plain")
            (a == a) shouldBe true
        }

        test("HttpBody.Bytes is not equal to other types") {
            val a = HttpBody.Bytes(byteArrayOf(1), "text/plain")
            (a.equals("not bytes")) shouldBe false
        }

        test("MultipartPart same reference is equal") {
            val a = MultipartPart("f", byteArrayOf(1), "text/plain")
            (a == a) shouldBe true
        }

        test("MultipartPart is not equal to other types") {
            val a = MultipartPart("f", byteArrayOf(1), "text/plain")
            (a.equals("not a part")) shouldBe false
        }

        test("HttpBody.Bytes hashCode same for equal content") {
            val a = HttpBody.Bytes(byteArrayOf(1, 2), "text/plain")
            val b = HttpBody.Bytes(byteArrayOf(1, 2), "text/plain")
            a.hashCode() shouldBe b.hashCode()
        }

        test("MultipartPart hashCode same for equal content") {
            val a = MultipartPart("f", byteArrayOf(1, 2), "text/plain", null)
            val b = MultipartPart("f", byteArrayOf(1, 2), "text/plain", null)
            a.hashCode() shouldBe b.hashCode()
        }
    })
