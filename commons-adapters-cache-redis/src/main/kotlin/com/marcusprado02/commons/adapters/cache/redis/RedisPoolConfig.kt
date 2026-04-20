package com.marcusprado02.commons.adapters.cache.redis

public data class RedisPoolConfig(
    val maxConnections: Int = 8,
    val minIdle: Int = 1,
    val maxIdle: Int = 4,
    val connectTimeoutMs: Long = 5_000L,
    val readTimeoutMs: Long = 10_000L,
)
