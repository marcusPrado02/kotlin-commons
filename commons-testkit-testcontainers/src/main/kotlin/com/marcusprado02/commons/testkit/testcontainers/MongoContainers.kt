package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.MongoDBContainer

/**
 * Provides a shared MongoDB Testcontainers instance for integration testing.
 *
 * The container is started lazily on first access and reused across tests.
 */
public object MongoContainers {
    /** Singleton MongoDB container (mongo 7). */
    public val instance: MongoDBContainer by lazy {
        MongoDBContainer("mongo:7").also { it.start() }
    }
}
