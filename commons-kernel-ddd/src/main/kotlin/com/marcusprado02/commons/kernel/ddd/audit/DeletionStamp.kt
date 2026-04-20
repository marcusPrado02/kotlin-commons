package com.marcusprado02.commons.kernel.ddd.audit

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import java.time.Instant

/**
 * Records the actor, time, and optional reason for a soft-delete operation.
 *
 * @property actorId the actor that requested the deletion.
 * @property at the instant the entity was soft-deleted.
 * @property reason optional human-readable reason for the deletion.
 */
public data class DeletionStamp(
    val actorId: ActorId,
    val at: Instant,
    val reason: String? = null,
)
