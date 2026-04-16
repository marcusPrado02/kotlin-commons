package com.marcusprado02.commons.ports.http

public data class HttpResponse<T>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: T?,
) {
    public val isSuccessful: Boolean get() = statusCode in SUCCESS_RANGE
    public val isClientError: Boolean get() = statusCode in CLIENT_ERROR_RANGE
    public val isServerError: Boolean get() = statusCode in SERVER_ERROR_RANGE

    public companion object {
        private val SUCCESS_RANGE = 200..299
        private val CLIENT_ERROR_RANGE = 400..499
        private val SERVER_ERROR_RANGE = 500..599
    }
}
