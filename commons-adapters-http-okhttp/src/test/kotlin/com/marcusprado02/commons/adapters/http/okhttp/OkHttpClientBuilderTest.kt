package com.marcusprado02.commons.adapters.http.okhttp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.time.Duration

class OkHttpClientBuilderTest :
    FunSpec({
        test("builder with default values compiles and builds successfully") {
            val client = OkHttpClientBuilder().build()
            client shouldNotBe null
        }

        test("builder produces OkHttpClient with correct connect timeout") {
            val client =
                OkHttpClientBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build()
            client.connectTimeoutMillis shouldBe 5_000
        }

        test("builder produces OkHttpClient with correct read timeout") {
            val client =
                OkHttpClientBuilder()
                    .readTimeout(Duration.ofSeconds(15))
                    .build()
            client.readTimeoutMillis shouldBe 15_000
        }

        test("builder produces OkHttpClient with correct write timeout") {
            val client =
                OkHttpClientBuilder()
                    .writeTimeout(Duration.ofSeconds(20))
                    .build()
            client.writeTimeoutMillis shouldBe 20_000
        }

        test("builder with all timeouts configured produces correct client") {
            val client =
                OkHttpClientBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .readTimeout(Duration.ofSeconds(10))
                    .writeTimeout(Duration.ofSeconds(10))
                    .maxIdleConnections(10)
                    .keepAliveDuration(Duration.ofMinutes(3))
                    .build()
            client.connectTimeoutMillis shouldBe 2_000
            client.readTimeoutMillis shouldBe 10_000
            client.writeTimeoutMillis shouldBe 10_000
        }

        test("builder with added LoggingInterceptor produces client (smoke test)") {
            val loggingInterceptor =
                Interceptor { chain ->
                    chain.proceed(chain.request())
                }
            val client: OkHttpClient =
                OkHttpClientBuilder()
                    .addInterceptor(loggingInterceptor)
                    .build()
            client.interceptors.size shouldBe 1
        }

        test("builder with multiple interceptors adds all to client") {
            val interceptor1 = Interceptor { chain -> chain.proceed(chain.request()) }
            val interceptor2 = Interceptor { chain -> chain.proceed(chain.request()) }
            val client =
                OkHttpClientBuilder()
                    .addInterceptor(interceptor1)
                    .addInterceptor(interceptor2)
                    .build()
            client.interceptors.size shouldBe 2
        }

        test("builder default values match expected defaults") {
            val client = OkHttpClientBuilder().build()
            client.connectTimeoutMillis shouldBe 10_000
            client.readTimeoutMillis shouldBe 30_000
            client.writeTimeoutMillis shouldBe 30_000
        }
    })
