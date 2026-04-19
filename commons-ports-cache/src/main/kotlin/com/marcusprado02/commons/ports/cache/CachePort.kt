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

public suspend inline fun <reified T : Any> CachePort.getOrPut(
    key: CacheKey,
    ttl: java.time.Duration? = null,
    crossinline loader: suspend () -> T,
): T {
    val cached = get<T>(key)
    if (cached != null) return cached
    val value = loader()
    put(key, value, ttl)
    return value
}

public suspend fun <T : Any> CachePort.getAll(
    keys: Set<CacheKey>,
    type: Class<T>,
): Map<CacheKey, T> {
    val result = mutableMapOf<CacheKey, T>()
    for (key in keys) {
        val v = get(key, type)
        if (v != null) result[key] = v
    }
    return result
}

public suspend inline fun <reified T : Any> CachePort.getAll(keys: Set<CacheKey>): Map<CacheKey, T> = getAll(keys, T::class.java)

@Suppress("UnusedParameter")
public suspend fun CachePort.invalidateByPrefix(prefix: String) {
    // This is a best-effort default that can be overridden by implementations
    // that have index knowledge. Default: no-op (implementations override as needed).
}
