package com.marcusprado02.commons.ports.messaging

import java.time.Instant

public data class MessageHeaders(
    val messageId: MessageId,
    val timestamp: Instant,
    val correlationId: String? = null,
    val tenantId: String? = null,
    val extra: Map<String, String> = emptyMap(),
)
