package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.GenericContainer

public object GreenMailContainers {
    private const val SMTP_PORT = 3025
    private const val IMAP_PORT = 3143
    private const val POP3_PORT = 3110

    /** SMTP: 3025, IMAP: 3143, POP3: 3110 */
    public val instance: GenericContainer<*> by lazy {
        GenericContainer("greenmail/standalone:2.1.2")
            .withExposedPorts(SMTP_PORT, IMAP_PORT, POP3_PORT)
            .also { it.start() }
    }
}
