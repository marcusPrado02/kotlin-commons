package com.marcusprado02.commons.ports.cache

import java.time.Duration

/** Port for interacting with a key-value cache store. */
public interface CachePort {
    /**
     * Returns the cached value for [key] as [type], or `null` if not present.
     *
     * @param key cache key to look up.
     * @param type the expected Java class of the cached value.
     */
    public suspend fun <T : Any> get(
        key: CacheKey,
        type: Class<T>,
    ): T?

    /**
     * Stores [value] under [key] with an optional TTL.
     *
     * @param key cache key.
     * @param value value to store.
     * @param ttl optional time-to-live; `null` means the entry does not expire.
     */
    public suspend fun <T : Any> put(
        key: CacheKey,
        value: T,
        ttl: Duration? = null,
    )

    /** Removes the entry for [key], if present. */
    public suspend fun remove(key: CacheKey)

    /** Removes all entries from the cache. */
    public suspend fun clear()

    /** Returns `true` if an entry exists for [key]. */
    public suspend fun exists(key: CacheKey): Boolean
}

/** Reified helper to avoid passing `Class<T>` explicitly. */
public suspend inline fun <reified T : Any> CachePort.get(key: CacheKey): T? = get(key, T::class.java)

/**
 * Returns the cached value for [key], or stores and returns the result of [loader] if absent.
 *
 * @param key cache key to look up or populate.
 * @param ttl optional TTL for the stored value.
 * @param loader function invoked when the cache misses; its result is stored before returning.
 */
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

/**
 * Fetches all values for the given [keys], returning only those present in the cache.
 *
 * @param keys set of keys to look up.
 * @param type the expected Java class of each cached value.
 */
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

/** Reified variant of [getAll] that infers the type from the type parameter. */
public suspend inline fun <reified T : Any> CachePort.getAll(keys: Set<CacheKey>): Map<CacheKey, T> = getAll(keys, T::class.java)

/**
 * Best-effort prefix-based invalidation; the default implementation is a no-op.
 * Cache adapters that maintain a key index should override this via extension or delegation.
 *
 * @param prefix key prefix used to identify entries to invalidate.
 */
@Suppress("UnusedParameter")
public suspend fun CachePort.invalidateByPrefix(prefix: String) {
    // This is a best-effort default that can be overridden by implementations
    // that have index knowledge. Default: no-op (implementations override as needed).
}
