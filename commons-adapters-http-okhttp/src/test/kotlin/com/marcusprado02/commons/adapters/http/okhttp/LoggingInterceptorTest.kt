package com.marcusprado02.commons.adapters.http.okhttp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class LoggingInterceptorTest :
    FunSpec({
        val server = MockWebServer()

        beforeSpec { server.start() }
        afterSpec { server.shutdown() }

        test("LoggingInterceptor passes through requests without blocking") {
            server.enqueue(MockResponse().setBody("ok").setResponseCode(200))
            val client =
                OkHttpClient
                    .Builder()
                    .addInterceptor(LoggingInterceptor())
                    .build()
            val response =
                client
                    .newCall(
                        Request.Builder().url(server.url("/")).build(),
                    ).execute()
            response.code shouldBe 200
            response.body?.string() shouldBe "ok"
            response.close()
        }

        test("LoggingInterceptor passes through non-200 responses") {
            server.enqueue(MockResponse().setResponseCode(404))
            val client =
                OkHttpClient
                    .Builder()
                    .addInterceptor(LoggingInterceptor())
                    .build()
            val response =
                client
                    .newCall(
                        Request.Builder().url(server.url("/missing")).build(),
                    ).execute()
            response.code shouldBe 404
            response.close()
        }
    })
