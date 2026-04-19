package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.MySQLContainer

public object MySqlContainers {
    public val instance: MySQLContainer<*> by lazy {
        MySQLContainer("mysql:8").also { it.start() }
    }
}
