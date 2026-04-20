@file:Suppress("DEPRECATION")

package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

public object KafkaContainers {
    private const val SCHEMA_REGISTRY_PORT = 8081

    public val instance: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .also { it.start() }
    }

    public val schemaRegistry: org.testcontainers.containers.GenericContainer<*> by lazy {
        val kafka = instance
        @Suppress("DEPRECATION")
        org.testcontainers.containers
            .GenericContainer("confluentinc/cp-schema-registry:7.6.0")
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:$SCHEMA_REGISTRY_PORT")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", kafka.bootstrapServers)
            .withExposedPorts(SCHEMA_REGISTRY_PORT)
            .also { it.start() }
    }

    public val schemaRegistryUrl: String
        get() = "http://${schemaRegistry.host}:${schemaRegistry.getMappedPort(SCHEMA_REGISTRY_PORT)}"
}
