package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.aggregate.AggregateRoot
import com.marcusprado02.commons.kernel.ddd.aggregate.AggregateSnapshot
import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.event.DomainEvent
import com.marcusprado02.commons.kernel.ddd.event.EventId
import com.marcusprado02.commons.kernel.ddd.event.EventMetadata
import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Instant

private val TENANT = TenantId("tenant-1")
private val ACTOR = ActorId("actor-1")
private val NOW = Instant.parse("2026-01-01T00:00:00Z")
private val STAMP = AuditStamp(ACTOR, NOW)
private val TRAIL = AuditTrail(STAMP, STAMP)

private data class NameChanged(
    override val eventId: EventId,
    override val occurredAt: Instant,
    override val aggregateType: String,
    override val aggregateId: String,
    override val aggregateVersion: EntityVersion,
    override val metadata: EventMetadata,
    val newName: String,
) : DomainEvent

private class OrderAggregate(id: String, tenantId: TenantId, audit: AuditTrail, var name: String) :
    AggregateRoot<String>(id, tenantId, EntityVersion.INITIAL, audit) {

    fun rename(newName: String, stamp: AuditStamp, meta: EventMetadata) {
        recordChange(stamp, mutation = { name = newName }) { snapshot ->
            NameChanged(
                eventId = EventId.generate(),
                occurredAt = stamp.at,
                aggregateType = "OrderAggregate",
                aggregateId = snapshot.aggregateId,
                aggregateVersion = snapshot.version,
                metadata = meta,
                newName = newName,
            )
        }
    }
}

class AggregateRootTest : FunSpec({
    val meta = EventMetadata(
        correlationId = CorrelationId.generate(),
        tenantId = TENANT,
        actorId = ACTOR,
    )

    test("recordChange mutates state and records event") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        order.rename("Order Renamed", STAMP, meta)

        order.name shouldBe "Order Renamed"
        order.version shouldBe EntityVersion(1L)
        order.peekDomainEvents() shouldHaveSize 1
    }

    test("pullDomainEvents returns and clears events") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        order.rename("Renamed", STAMP, meta)

        val events = order.pullDomainEvents()
        events shouldHaveSize 1
        (events[0] as NameChanged).newName shouldBe "Renamed"
        order.peekDomainEvents() shouldHaveSize 0
    }

    test("snapshot returns current aggregate state") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        val snap = order.snapshot()

        snap.aggregateId shouldBe "o-1"
        snap.tenantId shouldBe TENANT
        snap.version shouldBe EntityVersion.INITIAL
        snap.aggregateType shouldBe "OrderAggregate"
    }

    test("multiple changes accumulate events") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        order.rename("Second", STAMP, meta)
        order.rename("Third", STAMP, meta)
        order.peekDomainEvents() shouldHaveSize 2
    }
})
