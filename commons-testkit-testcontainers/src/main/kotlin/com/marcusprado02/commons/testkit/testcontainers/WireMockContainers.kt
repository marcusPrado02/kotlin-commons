package com.marcusprado02.commons.testkit.testcontainers

import org.wiremock.integrations.testcontainers.WireMockContainer

public object WireMockContainers {
    public val instance: WireMockContainer by lazy {
        WireMockContainer("wiremock/wiremock:3.5.4").also { it.start() }
    }
}
