package com.marcusprado02.commons.kernel.time

import java.time.Clock
import java.time.Instant

public fun interface ClockProvider {
    public fun clock(): Clock
    public fun now(): Instant = clock().instant()
}
