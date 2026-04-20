package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.MySQLContainer

/**
 * Provides a shared MySQL Testcontainers instance for integration testing.
 *
 * The container is started lazily on first access and reused across tests.
 */
public object MySqlContainers {
    /** Singleton MySQL container (mysql 8). */
    public val instance: MySQLContainer<*> by lazy {
        MySQLContainer("mysql:8").also { it.start() }
    }
}
