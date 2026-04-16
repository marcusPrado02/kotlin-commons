package com.marcusprado02.commons.kernel.ddd.identity

@JvmInline
public value class TenantId(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "TenantId must not be blank" }
    }

    override fun toString(): String = value
}
