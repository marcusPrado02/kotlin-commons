package com.marcusprado02.commons.kernel.ddd.event

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public data class EventMetadata(
    val correlationId: CorrelationId,
    val tenantId: TenantId,
    val actorId: ActorId?,
    val extra: Map<String, String> = emptyMap(),
)
