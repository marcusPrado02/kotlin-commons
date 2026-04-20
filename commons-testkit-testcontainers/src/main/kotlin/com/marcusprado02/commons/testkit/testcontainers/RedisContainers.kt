package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.GenericContainer

/**
 * Provides a shared Redis Testcontainers instance for integration testing.
 *
 * The container is started lazily on first access and reused across tests.
 */
public object RedisContainers {
    private const val REDIS_PORT = 6379

    /** Singleton Redis container (redis 7-alpine) exposing port 6379. */
    public val instance: GenericContainer<*> by lazy {
        GenericContainer("redis:7-alpine")
            .withExposedPorts(REDIS_PORT)
            .also { it.start() }
    }
}
