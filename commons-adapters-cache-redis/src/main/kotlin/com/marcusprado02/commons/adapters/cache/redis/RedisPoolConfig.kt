package com.marcusprado02.commons.adapters.cache.redis

/**
 * Configuration parameters for the Redis connection pool.
 *
 * @property maxConnections maximum number of connections in the pool.
 * @property minIdle minimum number of idle connections to maintain.
 * @property maxIdle maximum number of idle connections to keep.
 * @property connectTimeoutMs connection establishment timeout in milliseconds.
 * @property readTimeoutMs socket read timeout in milliseconds.
 */
public data class RedisPoolConfig(
    val maxConnections: Int = 8,
    val minIdle: Int = 1,
    val maxIdle: Int = 4,
    val connectTimeoutMs: Long = 5_000L,
    val readTimeoutMs: Long = 10_000L,
)
