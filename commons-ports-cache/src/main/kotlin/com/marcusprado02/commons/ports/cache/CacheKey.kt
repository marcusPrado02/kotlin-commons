package com.marcusprado02.commons.ports.cache

/** Typed, non-blank string identifier for a cache entry. */
@JvmInline
public value class CacheKey(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "CacheKey must not be blank" }
    }

    override fun toString(): String = value
}
