package com.marcusprado02.commons.kernel.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

public class FixedClockProvider(
    private val fixedInstant: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) : ClockProvider {
    override fun clock(): Clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    public fun advance(duration: java.time.Duration): FixedClockProvider = FixedClockProvider(clock().instant().plus(duration))
}
