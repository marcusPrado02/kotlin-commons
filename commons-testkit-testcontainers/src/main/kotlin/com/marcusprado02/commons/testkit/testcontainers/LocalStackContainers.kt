package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

/**
 * Provides a shared LocalStack Testcontainers instance for integration testing of AWS service interactions.
 *
 * The container is started lazily on first access and reused across tests.
 */
public object LocalStackContainers {
    /** Singleton LocalStack container (localstack 3.4). */
    public val instance: LocalStackContainer by lazy {
        LocalStackContainer(DockerImageName.parse("localstack/localstack:3.4"))
            .also { it.start() }
    }
}
