package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.command.Command
import com.marcusprado02.commons.kernel.ddd.command.CommandHandler
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.event.DomainEvent
import com.marcusprado02.commons.kernel.ddd.event.EventId
import com.marcusprado02.commons.kernel.ddd.event.EventMetadata
import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId
import com.marcusprado02.commons.kernel.ddd.policy.Policy
import com.marcusprado02.commons.kernel.ddd.query.Query
import com.marcusprado02.commons.kernel.ddd.query.QueryHandler
import com.marcusprado02.commons.kernel.ddd.service.DomainService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import java.time.Instant

private data class AddItemCommand(
    val name: String,
) : Command

private class AddItemHandler : CommandHandler<AddItemCommand, String> {
    override suspend fun handle(command: AddItemCommand): String = "Added: ${command.name}"
}

private data class FindByIdQuery(
    val id: Int,
) : Query<String?>

private class FindByIdHandler : QueryHandler<FindByIdQuery, String?> {
    override suspend fun handle(query: FindByIdQuery): String? = "item-${query.id}"
}

private class PricingService : DomainService

private data class SampleEvent(
    override val eventId: EventId,
    override val occurredAt: Instant,
    override val aggregateType: String,
    override val aggregateId: String,
    override val aggregateVersion: EntityVersion,
    override val metadata: EventMetadata,
) : DomainEvent

class CqrsAbstractionsTest :
    FunSpec({
        test("CommandHandler returns expected result") {
            runTest {
                val handler = AddItemHandler()
                val result = handler.handle(AddItemCommand("Widget"))
                result shouldBe "Added: Widget"
            }
        }

        test("QueryHandler returns expected result") {
            runTest {
                val handler = FindByIdHandler()
                val result = handler.handle(FindByIdQuery(42))
                result shouldBe "item-42"
            }
        }

        test("DomainService marker interface is implemented") {
            val service = PricingService()
            service.shouldBeInstanceOf<DomainService>()
        }

        test("Policy interface can be implemented and invoked") {
            runTest {
                val meta =
                    EventMetadata(
                        correlationId = CorrelationId.generate(),
                        tenantId = TenantId("tenant-1"),
                        actorId = ActorId("actor-1"),
                    )
                val event =
                    SampleEvent(
                        eventId = EventId.generate(),
                        occurredAt = Instant.now(),
                        aggregateType = "Sample",
                        aggregateId = "agg-1",
                        aggregateVersion = EntityVersion.INITIAL,
                        metadata = meta,
                    )
                var handled = false
                val policy =
                    object : Policy<SampleEvent> {
                        override suspend fun handle(event: SampleEvent) {
                            handled = true
                        }
                    }
                policy.handle(event)
                handled shouldBe true
            }
        }
    })
