package com.marcusprado02.commons.ports.http

public interface HttpClientPort {
    public suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>

    public suspend fun <T> execute(
        request: HttpRequest,
        mapper: (ByteArray) -> T,
    ): HttpResponse<T>
}
