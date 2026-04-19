package com.marcusprado02.commons.ports.http

import java.net.URI

public interface HttpClientPort {
    public suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>

    public suspend fun <T> execute(
        request: HttpRequest,
        mapper: (ByteArray) -> T,
    ): HttpResponse<T>
}

public suspend fun HttpClientPort.get(uri: URI): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.GET))

public suspend fun HttpClientPort.post(
    uri: URI,
    body: HttpBody,
): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.POST, body = body))

public suspend fun HttpClientPort.put(
    uri: URI,
    body: HttpBody,
): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.PUT, body = body))

public suspend fun HttpClientPort.delete(uri: URI): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.DELETE))
