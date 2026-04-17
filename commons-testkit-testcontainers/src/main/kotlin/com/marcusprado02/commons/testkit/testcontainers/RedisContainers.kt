package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.GenericContainer

object RedisContainers {
    val instance: GenericContainer<*> by lazy {
        GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)
            .also { it.start() }
    }
}
