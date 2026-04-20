package com.marcusprado02.commons.kernel.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * [ClockProvider] backed by a fixed [Instant], intended for deterministic testing.
 *
 * @param fixedInstant the instant that [clock] will always report; defaults to `2026-01-01T00:00:00Z`.
 */
public class FixedClockProvider(
    private val fixedInstant: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) : ClockProvider {
    override fun clock(): Clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    /**
     * Returns a new [FixedClockProvider] with the fixed instant advanced by [duration].
     *
     * @param duration the amount of time to add.
     */
    public fun advance(duration: java.time.Duration): FixedClockProvider = FixedClockProvider(clock().instant().plus(duration))
}
