package com.marcusprado02.commons.kernel.ddd.aggregate

import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

/**
 * Immutable point-in-time snapshot of an aggregate's identity and version.
 *
 * Passed to domain event factories so events carry consistent aggregate metadata.
 *
 * @param I the type of the aggregate's identity.
 */
public data class AggregateSnapshot<I : Any>(
    val aggregateId: I,
    val tenantId: TenantId,
    val version: EntityVersion,
    val aggregateType: String,
)
