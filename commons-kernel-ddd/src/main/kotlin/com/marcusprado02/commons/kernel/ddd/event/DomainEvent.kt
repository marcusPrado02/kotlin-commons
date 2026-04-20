package com.marcusprado02.commons.kernel.ddd.event

import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import java.time.Instant

/**
 * Contract for all domain events raised by aggregates.
 *
 * Implementations carry the aggregate's identity, version, and cross-cutting metadata
 * required for event sourcing, auditing, and messaging.
 */
public interface DomainEvent {
    /** Unique identifier for this event instance. */
    public val eventId: EventId

    /** The instant at which the event occurred within the domain. */
    public val occurredAt: Instant

    /** Simple class name of the aggregate that raised this event. */
    public val aggregateType: String

    /** String representation of the aggregate's identity. */
    public val aggregateId: String

    /** Aggregate version at the time the event was raised. */
    public val aggregateVersion: EntityVersion

    /** Cross-cutting metadata such as correlation and tenant. */
    public val metadata: EventMetadata
}
