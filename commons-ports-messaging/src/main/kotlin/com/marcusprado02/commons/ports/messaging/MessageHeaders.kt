package com.marcusprado02.commons.ports.messaging

import java.time.Instant

/**
 * Standard and custom headers carried by a message.
 *
 * @property messageId unique identifier of this message.
 * @property timestamp when the message was created.
 * @property correlationId optional identifier linking related messages across services.
 * @property tenantId optional tenant scope for multi-tenant systems.
 * @property extra arbitrary additional headers.
 */
public data class MessageHeaders(
    val messageId: MessageId,
    val timestamp: Instant,
    val correlationId: String? = null,
    val tenantId: String? = null,
    val extra: Map<String, String> = emptyMap(),
)
