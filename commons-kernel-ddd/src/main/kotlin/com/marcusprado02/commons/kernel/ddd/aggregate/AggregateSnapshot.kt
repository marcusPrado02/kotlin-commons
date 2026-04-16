package com.marcusprado02.commons.kernel.ddd.aggregate

import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public data class AggregateSnapshot<I : Any>(
    val aggregateId: I,
    val tenantId: TenantId,
    val version: EntityVersion,
    val aggregateType: String,
)
