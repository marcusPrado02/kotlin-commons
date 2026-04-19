@file:Suppress("DEPRECATION")

package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

public object KafkaContainers {
    public val instance: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .also { it.start() }
    }
}
