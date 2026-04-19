package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpResponse
import okhttp3.Response

internal fun Response.toHttpResponse(): HttpResponse<ByteArray> = use { resp ->
    HttpResponse(
        statusCode = resp.code,
        headers = resp.headers.toMultimap(),
        body = resp.body?.bytes(),
    )
}

internal fun <T> HttpResponse<ByteArray>.map(mapper: (ByteArray) -> T): HttpResponse<T> =
    HttpResponse(
        statusCode = statusCode,
        headers = headers,
        body = body?.let(mapper),
    )
