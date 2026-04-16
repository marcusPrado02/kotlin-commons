package com.marcusprado02.commons.kernel.ddd.audit

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import java.time.Instant

public data class DeletionStamp(
    val actorId: ActorId,
    val at: Instant,
    val reason: String? = null,
)
