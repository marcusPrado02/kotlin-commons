package com.marcusprado02.commons.kernel.ddd.identity

/**
 * Identifies a tenant in a multi-tenant system.
 *
 * @property value the non-blank string identifying the tenant.
 */
@JvmInline
public value class TenantId(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "TenantId must not be blank" }
    }

    override fun toString(): String = value
}
