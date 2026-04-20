package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.PostgreSQLContainer

/**
 * Provides a shared PostgreSQL Testcontainers instance for integration testing.
 *
 * The container is started lazily on first access and reused across tests.
 */
public object PostgresContainers {
    /** Singleton PostgreSQL container (postgres 16-alpine). */
    public val instance: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:16-alpine").also { it.start() }
    }
}
