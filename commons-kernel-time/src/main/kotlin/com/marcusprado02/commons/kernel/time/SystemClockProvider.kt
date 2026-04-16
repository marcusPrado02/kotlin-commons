package com.marcusprado02.commons.kernel.time

import java.time.Clock

public object SystemClockProvider : ClockProvider {
    override fun clock(): Clock = Clock.systemUTC()
}
