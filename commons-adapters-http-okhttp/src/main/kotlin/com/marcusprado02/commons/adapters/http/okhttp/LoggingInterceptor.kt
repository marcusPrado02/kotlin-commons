package com.marcusprado02.commons.adapters.http.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.LoggerFactory

public class LoggingInterceptor : Interceptor {
    private val log = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.currentTimeMillis()
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - start
        log.info("HTTP {} {} -> {} ({}ms)", request.method, request.url, response.code, duration)
        return response
    }
}
