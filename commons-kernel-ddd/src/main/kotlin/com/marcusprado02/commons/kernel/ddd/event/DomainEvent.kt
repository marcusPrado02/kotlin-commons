package com.marcusprado02.commons.kernel.ddd.event

import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import java.time.Instant

public interface DomainEvent {
    public val eventId: EventId
    public val occurredAt: Instant
    public val aggregateType: String
    public val aggregateId: String
    public val aggregateVersion: EntityVersion
    public val metadata: EventMetadata
}
