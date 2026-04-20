package com.marcusprado02.commons.kernel.time

import java.time.Clock

/** [ClockProvider] backed by the system UTC clock; use as the production singleton. */
public object SystemClockProvider : ClockProvider {
    override fun clock(): Clock = Clock.systemUTC()
}
