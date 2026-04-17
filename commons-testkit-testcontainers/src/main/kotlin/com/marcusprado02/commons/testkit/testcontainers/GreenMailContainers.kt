package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.GenericContainer

object GreenMailContainers {
    /** SMTP: 3025, IMAP: 3143, POP3: 3110 */
    val instance: GenericContainer<*> by lazy {
        GenericContainer("greenmail/standalone:2.1.2")
            .withExposedPorts(3025, 3143, 3110)
            .also { it.start() }
    }
}
