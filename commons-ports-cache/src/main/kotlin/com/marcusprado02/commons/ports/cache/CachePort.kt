package com.marcusprado02.commons.ports.cache

import java.time.Duration

public interface CachePort {
    public suspend fun <T : Any> get(
        key: CacheKey,
        type: Class<T>,
    ): T?

    public suspend fun <T : Any> put(
        key: CacheKey,
        value: T,
        ttl: Duration? = null,
    )

    public suspend fun remove(key: CacheKey)

    public suspend fun clear()

    public suspend fun exists(key: CacheKey): Boolean
}

/** Reified helper to avoid passing Class<T> explicitly. */
public suspend inline fun <reified T : Any> CachePort.get(key: CacheKey): T? = get(key, T::class.java)
