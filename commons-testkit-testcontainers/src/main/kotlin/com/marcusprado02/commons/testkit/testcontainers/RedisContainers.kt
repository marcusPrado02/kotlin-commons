package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.GenericContainer

public object RedisContainers {
    private const val REDIS_PORT = 6379

    public val instance: GenericContainer<*> by lazy {
        GenericContainer("redis:7-alpine")
            .withExposedPorts(REDIS_PORT)
            .also { it.start() }
    }
}
