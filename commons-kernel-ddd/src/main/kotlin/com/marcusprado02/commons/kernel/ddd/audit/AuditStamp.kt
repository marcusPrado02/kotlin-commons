package com.marcusprado02.commons.kernel.ddd.audit

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import java.time.Instant

/**
 * Records who performed an action and when.
 *
 * @property actorId the actor that performed the action.
 * @property at the instant the action occurred.
 */
public data class AuditStamp(
    val actorId: ActorId,
    val at: Instant,
)
