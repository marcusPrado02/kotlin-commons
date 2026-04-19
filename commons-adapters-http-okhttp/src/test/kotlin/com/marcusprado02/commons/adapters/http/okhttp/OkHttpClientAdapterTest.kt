package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpBody
import com.marcusprado02.commons.ports.http.HttpMethod
import com.marcusprado02.commons.ports.http.HttpRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

class OkHttpClientAdapterTest :
    FunSpec({
        val server = MockWebServer()
        val client = OkHttpClient()
        val adapter = OkHttpClientAdapter(client)

        beforeSpec { server.start() }
        afterSpec { server.shutdown() }
        afterEach { repeat(server.requestCount) { server.takeRequest(0, TimeUnit.MILLISECONDS) } }

        fun url(path: String = "/") = URI.create(server.url(path).toString())

        test("GET request returns 200 with body") {
            runTest {
                server.enqueue(MockResponse().setBody("hello").setResponseCode(200))
                val response = adapter.execute(HttpRequest(url(), HttpMethod.GET))
                response.statusCode shouldBe 200
                String(response.body!!) shouldBe "hello"
            }
        }

        test("POST request with JSON body") {
            runTest {
                server.enqueue(MockResponse().setResponseCode(201))
                val request =
                    HttpRequest(
                        uri = url("/items"),
                        method = HttpMethod.POST,
                        body = HttpBody.Bytes("""{"name":"test"}""".toByteArray(), "application/json"),
                    )
                val response = adapter.execute(request)
                response.statusCode shouldBe 201
                val recorded = server.takeRequest()
                recorded.method shouldBe "POST"
                String(recorded.body.readByteArray()) shouldBe """{"name":"test"}"""
            }
        }

        test("execute with mapper transforms body") {
            runTest {
                server.enqueue(MockResponse().setBody("42").setResponseCode(200))
                val response =
                    adapter.execute(HttpRequest(url(), HttpMethod.GET)) { bytes ->
                        String(bytes).trim().toInt()
                    }
                response.statusCode shouldBe 200
                response.body shouldBe 42
            }
        }

        test("404 response is client error") {
            runTest {
                server.enqueue(MockResponse().setResponseCode(404))
                val response = adapter.execute(HttpRequest(url("/missing"), HttpMethod.GET))
                response.isClientError shouldBe true
                response.isSuccessful shouldBe false
            }
        }

        test("POST with FormUrlEncoded body") {
            runTest {
                server.enqueue(MockResponse().setResponseCode(200))
                val request =
                    HttpRequest(
                        uri = url("/form"),
                        method = HttpMethod.POST,
                        body = HttpBody.FormUrlEncoded(mapOf("key" to "value")),
                    )
                adapter.execute(request)
                val recorded = server.takeRequest()
                recorded.body.readUtf8() shouldBe "key=value"
            }
        }

        test("response headers are mapped") {
            runTest {
                server.enqueue(MockResponse().setResponseCode(200).addHeader("X-Custom", "yes"))
                val response = adapter.execute(HttpRequest(url(), HttpMethod.GET))
                response.headers["x-custom"] shouldNotBe null
            }
        }

        test("propagates IOException on connection failure") {
            runTest {
                val deadServer = MockWebServer()
                deadServer.start()
                val deadUrl = URI.create(deadServer.url("/").toString())
                deadServer.shutdown()
                val request = HttpRequest(deadUrl, HttpMethod.GET)
                try {
                    adapter.execute(request)
                    error("Expected IOException")
                } catch (_: IOException) {
                    // expected — IOException is the success condition
                }
            }
        }
    })
