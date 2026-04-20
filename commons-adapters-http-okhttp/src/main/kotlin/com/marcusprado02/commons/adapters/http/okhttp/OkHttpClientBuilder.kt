package com.marcusprado02.commons.adapters.http.okhttp

import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Fluent builder for constructing a pre-configured [OkHttpClient].
 *
 * Defaults: connect timeout 10 s, read/write timeout 30 s, max idle connections 5, keep-alive 5 min.
 */
public class OkHttpClientBuilder {
    private companion object {
        private const val DEFAULT_CONNECT_TIMEOUT_SECONDS = 10L
        private const val DEFAULT_READ_TIMEOUT_SECONDS = 30L
        private const val DEFAULT_WRITE_TIMEOUT_SECONDS = 30L
        private const val DEFAULT_MAX_IDLE_CONNECTIONS = 5
        private const val DEFAULT_KEEP_ALIVE_MINUTES = 5L
    }

    private var connectTimeout: Duration = Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS)
    private var readTimeout: Duration = Duration.ofSeconds(DEFAULT_READ_TIMEOUT_SECONDS)
    private var writeTimeout: Duration = Duration.ofSeconds(DEFAULT_WRITE_TIMEOUT_SECONDS)
    private var maxIdleConnections: Int = DEFAULT_MAX_IDLE_CONNECTIONS
    private var keepAliveDuration: Duration = Duration.ofMinutes(DEFAULT_KEEP_ALIVE_MINUTES)
    private val interceptors: MutableList<Interceptor> = mutableListOf()

    /** Sets the connection establishment timeout. */
    public fun connectTimeout(duration: Duration): OkHttpClientBuilder = apply { connectTimeout = duration }

    /** Sets the socket read timeout. */
    public fun readTimeout(duration: Duration): OkHttpClientBuilder = apply { readTimeout = duration }

    /** Sets the socket write timeout. */
    public fun writeTimeout(duration: Duration): OkHttpClientBuilder = apply { writeTimeout = duration }

    /** Sets the maximum number of idle connections to keep in the pool. */
    public fun maxIdleConnections(count: Int): OkHttpClientBuilder = apply { maxIdleConnections = count }

    /** Sets the duration an idle connection is kept alive in the pool. */
    public fun keepAliveDuration(duration: Duration): OkHttpClientBuilder = apply { keepAliveDuration = duration }

    /**
     * Adds an application-level interceptor.
     *
     * @param interceptor the interceptor to add.
     */
    public fun addInterceptor(interceptor: Interceptor): OkHttpClientBuilder = apply { interceptors += interceptor }

    /** Builds and returns the configured [OkHttpClient]. */
    public fun build(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeout.toMillis(), TimeUnit.MILLISECONDS)
            .connectionPool(
                ConnectionPool(maxIdleConnections, keepAliveDuration.toMillis(), TimeUnit.MILLISECONDS),
            ).also { builder -> interceptors.forEach { builder.addInterceptor(it) } }
            .build()
}
