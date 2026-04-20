package com.marcusprado02.commons.adapters.http.okhttp

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException

class CircuitBreakerInterceptorTest :
    FunSpec({
        val server = MockWebServer()

        beforeSpec { server.start() }
        afterSpec { server.shutdown() }

        fun closedCircuitBreaker(): CircuitBreaker =
            CircuitBreaker.of(
                "test",
                CircuitBreakerConfig.custom()
                    .slidingWindowSize(10)
                    .build(),
            )

        test("passes successful response through and records success") {
            val cb = closedCircuitBreaker()
            val client =
                OkHttpClient.Builder()
                    .addInterceptor(CircuitBreakerInterceptor(cb))
                    .build()
            server.enqueue(MockResponse().setBody("ok").setResponseCode(200))

            val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()

            response.code shouldBe 200
            response.close()
            cb.metrics.numberOfSuccessfulCalls shouldBe 1
        }

        test("records failure and rethrows IOException") {
            val cb = closedCircuitBreaker()
            val deadServer = MockWebServer()
            deadServer.start()
            val deadUrl = deadServer.url("/")
            deadServer.shutdown()

            val client =
                OkHttpClient.Builder()
                    .addInterceptor(CircuitBreakerInterceptor(cb))
                    .build()

            shouldThrow<IOException> {
                client.newCall(Request.Builder().url(deadUrl).build()).execute()
            }
            cb.metrics.numberOfFailedCalls shouldBe 1
        }

        test("throws IOException when circuit is OPEN") {
            val cb =
                CircuitBreaker.of(
                    "open-cb",
                    CircuitBreakerConfig.custom()
                        .slidingWindowSize(1)
                        .failureRateThreshold(100f)
                        .build(),
                )
            // Force the circuit open by recording a failure
            cb.onError(0, java.util.concurrent.TimeUnit.NANOSECONDS, IOException("seed failure"))
            cb.transitionToOpenState()

            val client =
                OkHttpClient.Builder()
                    .addInterceptor(CircuitBreakerInterceptor(cb))
                    .build()
            server.enqueue(MockResponse().setBody("should not reach").setResponseCode(200))

            val ex =
                shouldThrow<IOException> {
                    client.newCall(Request.Builder().url(server.url("/")).build()).execute()
                }
            ex.message shouldContain "OPEN"
        }
    })
