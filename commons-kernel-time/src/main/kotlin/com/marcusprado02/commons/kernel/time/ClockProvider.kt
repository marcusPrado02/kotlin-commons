package com.marcusprado02.commons.kernel.time

import java.time.Clock
import java.time.Instant

/**
 * Source of a [Clock] instance, used to abstract time in domain and application services.
 * Implement with [SystemClockProvider] in production and [FixedClockProvider] in tests.
 */
public fun interface ClockProvider {
    /** Returns the underlying [Clock]. */
    public fun clock(): Clock

    /** Returns the current [Instant] from [clock]. */
    public fun now(): Instant = clock().instant()
}
