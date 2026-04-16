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
    })
