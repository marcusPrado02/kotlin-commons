package com.marcusprado02.commons.ports.http

import java.net.URI
import java.time.Duration

/**
 * Description of an outgoing HTTP request.
 *
 * @property uri the target endpoint.
 * @property method HTTP method to use.
 * @property headers request headers.
 * @property body optional request body.
 * @property timeout per-request timeout; `null` falls back to the client default.
 * @property name optional logical name for logging and tracing.
 */
public data class HttpRequest(
    val uri: URI,
    val method: HttpMethod,
    val headers: Map<String, String> = emptyMap(),
    val body: HttpBody? = null,
    val timeout: Duration? = null,
    val name: String? = null,
)
