package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.MongoDBContainer

public object MongoContainers {
    public val instance: MongoDBContainer by lazy {
        MongoDBContainer("mongo:7").also { it.start() }
    }
}
