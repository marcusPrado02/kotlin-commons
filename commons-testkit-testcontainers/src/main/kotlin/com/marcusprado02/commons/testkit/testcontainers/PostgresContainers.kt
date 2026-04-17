package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.PostgreSQLContainer

object PostgresContainers {
    val instance: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:16-alpine").also { it.start() }
    }
}
