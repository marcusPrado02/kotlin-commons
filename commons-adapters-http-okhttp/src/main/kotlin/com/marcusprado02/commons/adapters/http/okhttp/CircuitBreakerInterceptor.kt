package com.marcusprado02.commons.adapters.http.okhttp

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp [Interceptor] that protects outgoing requests with a Resilience4j [CircuitBreaker].
 *
 * When the circuit is OPEN, [CallNotPermittedException] is wrapped in an [IOException] and thrown
 * so that callers and upstream interceptors see a consistent failure type. Successful responses and
 * any [IOException] thrown by the chain are reported to the circuit breaker accordingly.
 *
 * @param circuitBreaker the Resilience4j circuit breaker instance to apply.
 */
public class CircuitBreakerInterceptor(
    private val circuitBreaker: CircuitBreaker,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!circuitBreaker.tryAcquirePermission()) {
            throw IOException("Circuit breaker '${circuitBreaker.name}' is OPEN — request rejected")
        }
        val start = System.nanoTime()
        return try {
            val response = chain.proceed(chain.request())
            val durationNanos = System.nanoTime() - start
            circuitBreaker.onSuccess(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS)
            response
        } catch (ex: IOException) {
            val durationNanos = System.nanoTime() - start
            circuitBreaker.onError(durationNanos, java.util.concurrent.TimeUnit.NANOSECONDS, ex)
            throw ex
        }
    }
}
