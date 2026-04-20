package com.marcusprado02.commons.ports.http

import java.net.URI

/** Port for executing outgoing HTTP requests. */
public interface HttpClientPort {
    /**
     * Executes [request] and returns the raw byte response.
     *
     * @param request the HTTP request to execute.
     */
    public suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>

    /**
     * Executes [request] and maps the response body with [mapper].
     *
     * @param request the HTTP request to execute.
     * @param mapper function that converts the raw response bytes to [T].
     */
    public suspend fun <T> execute(
        request: HttpRequest,
        mapper: (ByteArray) -> T,
    ): HttpResponse<T>
}

/** Convenience extension that issues a GET request to [uri]. */
public suspend fun HttpClientPort.get(uri: URI): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.GET))

/**
 * Convenience extension that issues a POST request to [uri] with [body].
 *
 * @param uri target URI.
 * @param body request body.
 */
public suspend fun HttpClientPort.post(
    uri: URI,
    body: HttpBody,
): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.POST, body = body))

/**
 * Convenience extension that issues a PUT request to [uri] with [body].
 *
 * @param uri target URI.
 * @param body request body.
 */
public suspend fun HttpClientPort.put(
    uri: URI,
    body: HttpBody,
): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.PUT, body = body))

/** Convenience extension that issues a DELETE request to [uri]. */
public suspend fun HttpClientPort.delete(uri: URI): HttpResponse<ByteArray> = execute(HttpRequest(uri, HttpMethod.DELETE))
