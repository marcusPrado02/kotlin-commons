package com.marcusprado02.commons.kernel.ddd.aggregate

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.entity.Entity
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.event.DomainEvent
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

/**
 * Base class for DDD aggregate roots.
 *
 * Manages a list of pending [DomainEvent]s raised during state mutations and exposes
 * helper methods to record changes, soft-deletions, and restores atomically.
 *
 * @param I the type of the aggregate's identity.
 */
public abstract class AggregateRoot<I : Any>(
    id: I,
    tenantId: TenantId,
    initialVersion: EntityVersion = EntityVersion.INITIAL,
    initialAudit: AuditTrail,
    initialDeleted: Boolean = false,
    initialDeletion: DeletionStamp? = null,
) : Entity<I>(id, tenantId, initialVersion, initialAudit, initialDeleted, initialDeletion) {
    private val pendingEvents = mutableListOf<DomainEvent>()

    /**
     * Applies [mutation], advances the audit stamp, and enqueues the event produced by [event].
     *
     * @param updated the audit stamp for the change.
     * @param mutation side-effecting state mutation to apply.
     * @param event factory that builds a [DomainEvent] from the post-mutation snapshot.
     */
    protected fun recordChange(
        updated: AuditStamp,
        mutation: () -> Unit,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        mutation()
        touch(updated)
        pendingEvents += event(snapshot())
    }

    /**
     * Soft-deletes the aggregate and enqueues the event produced by [event].
     *
     * @param stamp deletion metadata (actor and timestamp).
     * @param updated the audit stamp for the operation.
     * @param event factory that builds a [DomainEvent] from the post-deletion snapshot.
     */
    protected fun recordSoftDelete(
        stamp: DeletionStamp,
        updated: AuditStamp,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        softDelete(stamp, updated)
        pendingEvents += event(snapshot())
    }

    /**
     * Restores a soft-deleted aggregate and enqueues the event produced by [event].
     *
     * @param updated the audit stamp for the restoration.
     * @param event factory that builds a [DomainEvent] from the post-restore snapshot.
     */
    protected fun recordRestore(
        updated: AuditStamp,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        restore(updated)
        pendingEvents += event(snapshot())
    }

    /** Returns all pending domain events and clears the internal queue. */
    public fun pullDomainEvents(): List<DomainEvent> {
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return events
    }

    /** Returns a snapshot of pending domain events without clearing the queue. */
    public fun peekDomainEvents(): List<DomainEvent> = pendingEvents.toList()

    /** Creates a snapshot of the aggregate's current identity and version. */
    public fun snapshot(): AggregateSnapshot<I> =
        AggregateSnapshot(
            aggregateId = id,
            tenantId = tenantId,
            version = version,
            aggregateType = this::class.simpleName ?: "Unknown",
        )
}
