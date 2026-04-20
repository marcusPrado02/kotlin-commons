package com.marcusprado02.commons.kernel.ddd.event

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

/**
 * Cross-cutting metadata attached to every [DomainEvent].
 *
 * @property correlationId links the event to an originating request or message.
 * @property tenantId identifies the tenant in whose context the event occurred.
 * @property actorId the actor that triggered the event, or `null` for system-initiated events.
 * @property extra arbitrary key-value pairs for extension without schema changes.
 */
public data class EventMetadata(
    val correlationId: CorrelationId,
    val tenantId: TenantId,
    val actorId: ActorId?,
    val extra: Map<String, String> = emptyMap(),
)
