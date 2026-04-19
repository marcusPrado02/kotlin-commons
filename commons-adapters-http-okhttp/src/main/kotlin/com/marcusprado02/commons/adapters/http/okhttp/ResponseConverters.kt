package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpResponse
import okhttp3.Response

internal fun Response.toHttpResponse(): HttpResponse<ByteArray> {
    val body = body?.bytes()
    close()
    return HttpResponse(
        statusCode = code,
        headers = headers.toMultimap(),
        body = body,
    )
}

internal fun <T> HttpResponse<ByteArray>.map(mapper: (ByteArray) -> T): HttpResponse<T> =
    HttpResponse(
        statusCode = statusCode,
        headers = headers,
        body = body?.let(mapper),
    )
