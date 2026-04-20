package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.TenantId

/** Resolves the tenant for the current execution context. */
public fun interface TenantProvider {
    /** Returns the [TenantId] for the active tenant. */
    public fun currentTenant(): TenantId
}

/**
 * [TenantProvider] that always returns the same [tenantId].
 *
 * Useful for single-tenant deployments and tests.
 */
public class FixedTenantProvider(
    private val tenantId: TenantId,
) : TenantProvider {
    override fun currentTenant(): TenantId = tenantId
}
