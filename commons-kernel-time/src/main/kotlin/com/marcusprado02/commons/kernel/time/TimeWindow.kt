package com.marcusprado02.commons.kernel.time

import java.time.Duration
import java.time.Instant

public data class TimeWindow(
    val start: Instant,
    val end: Instant,
) {
    init {
        require(!end.isBefore(start)) { "TimeWindow end must not be before start" }
    }

    public fun contains(instant: Instant): Boolean = !instant.isBefore(start) && !instant.isAfter(end)

    public fun duration(): Duration = Duration.between(start, end)

    public fun overlaps(other: TimeWindow): Boolean = start.isBefore(other.end) && end.isAfter(other.start)

    public companion object {
        public fun of(
            start: Instant,
            duration: Duration,
        ): TimeWindow = TimeWindow(start, start.plus(duration))
    }
}
