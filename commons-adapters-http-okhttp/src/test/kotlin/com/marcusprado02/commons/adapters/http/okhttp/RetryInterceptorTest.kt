package com.marcusprado02.commons.adapters.http.okhttp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptorTest :
    FunSpec({
        val server = MockWebServer()

        beforeSpec { server.start() }
        afterSpec { server.shutdown() }
        afterEach { repeat(server.requestCount) { server.takeRequest(0, TimeUnit.MILLISECONDS) } }

        test("RetryInterceptor succeeds on retry after initial server error") {
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setBody("success").setResponseCode(200))
            val client =
                OkHttpClient
                    .Builder()
                    .addInterceptor(RetryInterceptor(maxRetries = 3, delayMillis = 0L))
                    .build()
            val response =
                client
                    .newCall(
                        Request.Builder().url(server.url("/")).build(),
                    ).execute()
            response.code shouldBe 200
            response.body?.string() shouldBe "success"
            response.close()
        }

        test("RetryInterceptor returns last response after exhausting retries on non-successful status") {
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setResponseCode(503))
            val client =
                OkHttpClient
                    .Builder()
                    .addInterceptor(RetryInterceptor(maxRetries = 3, delayMillis = 0L))
                    .build()
            val response =
                client
                    .newCall(
                        Request.Builder().url(server.url("/")).build(),
                    ).execute()
            response.code shouldBe 503
            response.close()
        }

        test("RetryInterceptor throws IOException after exhausting retries on connection failure") {
            val deadServer = MockWebServer()
            deadServer.start()
            val deadUrl = deadServer.url("/")
            deadServer.shutdown()
            val client =
                OkHttpClient
                    .Builder()
                    .addInterceptor(RetryInterceptor(maxRetries = 2, delayMillis = 0L))
                    .build()
            shouldThrow<IOException> {
                client.newCall(Request.Builder().url(deadUrl).build()).execute()
            }
        }
    })
