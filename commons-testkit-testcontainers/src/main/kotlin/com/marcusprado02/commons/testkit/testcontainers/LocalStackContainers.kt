package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName

public object LocalStackContainers {
    public val instance: LocalStackContainer by lazy {
        LocalStackContainer(DockerImageName.parse("localstack/localstack:3.4"))
            .also { it.start() }
    }
}
