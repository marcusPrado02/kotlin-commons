package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public fun interface TenantProvider {
    public fun currentTenant(): TenantId
}

public class FixedTenantProvider(
    private val tenantId: TenantId,
) : TenantProvider {
    override fun currentTenant(): TenantId = tenantId
}
