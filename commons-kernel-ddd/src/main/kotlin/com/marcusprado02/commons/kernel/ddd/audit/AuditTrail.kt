package com.marcusprado02.commons.kernel.ddd.audit

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import java.time.Clock

/**
 * Tracks the creation, last-update, and optional soft-deletion of an entity.
 *
 * @property created stamp recorded when the entity was first persisted.
 * @property updated stamp recorded on the most recent mutation.
 * @property deleted stamp recorded when the entity was soft-deleted, or `null` if still active.
 */
public data class AuditTrail(
    val created: AuditStamp,
    val updated: AuditStamp,
    val deleted: DeletionStamp? = null,
) {
    /**
     * Returns a copy of this trail with [deleted] filled in.
     *
     * @param actor the actor requesting the deletion.
     * @param clock the clock used to record the deletion time.
     * @param reason optional human-readable reason for the deletion.
     */
    public fun markDeleted(
        actor: ActorId,
        clock: Clock,
        reason: String? = null,
    ): AuditTrail = copy(deleted = DeletionStamp(actor, clock.instant(), reason))
}
