package com.marcusprado02.commons.ports.http

/**
 * Response received from an HTTP call.
 *
 * @param T the type of the decoded response body.
 * @property statusCode HTTP status code.
 * @property headers response headers.
 * @property body decoded response body, or `null` if the response had no body.
 */
public data class HttpResponse<T>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: T?,
) {
    /** `true` if [statusCode] is in the 2xx range. */
    public val isSuccessful: Boolean get() = statusCode in SUCCESS_RANGE

    /** `true` if [statusCode] is in the 4xx range. */
    public val isClientError: Boolean get() = statusCode in CLIENT_ERROR_RANGE

    /** `true` if [statusCode] is in the 5xx range. */
    public val isServerError: Boolean get() = statusCode in SERVER_ERROR_RANGE

    public companion object {
        private val SUCCESS_RANGE = 200..299
        private val CLIENT_ERROR_RANGE = 400..499
        private val SERVER_ERROR_RANGE = 500..599
    }
}
