package com.marcusprado02.commons.ports.http

import java.net.URI
import java.time.Duration

public data class HttpRequest(
    val uri: URI,
    val method: HttpMethod,
    val headers: Map<String, String> = emptyMap(),
    val body: HttpBody? = null,
    val timeout: Duration? = null,
    val name: String? = null,
)
