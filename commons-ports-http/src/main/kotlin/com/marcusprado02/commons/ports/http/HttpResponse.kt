package com.marcusprado02.commons.ports.http

public data class HttpResponse<T>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: T?,
) {
    public val isSuccessful: Boolean get() = statusCode in 200..299
    public val isClientError: Boolean get() = statusCode in 400..499
    public val isServerError: Boolean get() = statusCode in 500..599
}
