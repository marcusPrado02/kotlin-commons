package com.marcusprado02.commons.kernel.ddd.aggregate

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.entity.Entity
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.event.DomainEvent
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public abstract class AggregateRoot<I : Any>(
    id: I,
    tenantId: TenantId,
    initialVersion: EntityVersion = EntityVersion.INITIAL,
    initialAudit: AuditTrail,
    initialDeleted: Boolean = false,
    initialDeletion: DeletionStamp? = null,
) : Entity<I>(id, tenantId, initialVersion, initialAudit, initialDeleted, initialDeletion) {
    private val pendingEvents = mutableListOf<DomainEvent>()

    protected fun recordChange(
        updated: AuditStamp,
        mutation: () -> Unit,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        mutation()
        touch(updated)
        pendingEvents += event(snapshot())
    }

    protected fun recordSoftDelete(
        stamp: DeletionStamp,
        updated: AuditStamp,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        softDelete(stamp, updated)
        pendingEvents += event(snapshot())
    }

    protected fun recordRestore(
        updated: AuditStamp,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        restore(updated)
        pendingEvents += event(snapshot())
    }

    public fun pullDomainEvents(): List<DomainEvent> {
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return events
    }

    public fun peekDomainEvents(): List<DomainEvent> = pendingEvents.toList()

    public fun snapshot(): AggregateSnapshot<I> =
        AggregateSnapshot(
            aggregateId = id,
            tenantId = tenantId,
            version = version,
            aggregateType = this::class.simpleName ?: "Unknown",
        )
}
