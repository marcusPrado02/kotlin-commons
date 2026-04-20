package com.marcusprado02.commons.testkit.testcontainers

import org.wiremock.integrations.testcontainers.WireMockContainer

/**
 * Provides a shared WireMock Testcontainers instance for HTTP stub-server testing.
 *
 * The container is started lazily on first access and reused across tests.
 */
public object WireMockContainers {
    /** Singleton WireMock container (wiremock 3.5.4). */
    public val instance: WireMockContainer by lazy {
        WireMockContainer("wiremock/wiremock:3.5.4").also { it.start() }
    }
}
