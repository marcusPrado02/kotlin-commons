package com.marcusprado02.commons.adapters.http.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

public class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val delayMillis: Long = 200L,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var lastException: IOException? = null
        repeat(maxRetries) { attempt ->
            try {
                val response = chain.proceed(chain.request().newBuilder().build())
                if (response.isSuccessful || attempt == maxRetries - 1) return response
                response.close()
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1) Thread.sleep(delayMillis)
            }
        }
        throw lastException ?: IOException("Request failed after $maxRetries attempts")
    }
}
