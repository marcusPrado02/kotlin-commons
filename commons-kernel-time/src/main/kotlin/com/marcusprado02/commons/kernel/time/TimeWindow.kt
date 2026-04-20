package com.marcusprado02.commons.kernel.time

import java.time.Duration
import java.time.Instant

/**
 * A closed time interval between [start] and [end] instants (inclusive on both ends).
 *
 * @property start the beginning of the window (must not be after [end]).
 * @property end the end of the window.
 */
public data class TimeWindow(
    val start: Instant,
    val end: Instant,
) {
    init {
        require(!end.isBefore(start)) { "TimeWindow end must not be before start" }
    }

    /**
     * Returns `true` if [instant] falls within this window (start and end are inclusive).
     *
     * @param instant the instant to test.
     */
    public fun contains(instant: Instant): Boolean = !instant.isBefore(start) && !instant.isAfter(end)

    /** Returns the [Duration] between [start] and [end]. */
    public fun duration(): Duration = Duration.between(start, end)

    /**
     * Returns `true` if this window and [other] share any common instant.
     *
     * @param other the window to check against.
     */
    public fun overlaps(other: TimeWindow): Boolean = start.isBefore(other.end) && end.isAfter(other.start)

    /**
     * Returns the smallest [TimeWindow] that contains both this window and [other].
     *
     * @param other the window to merge with.
     */
    public fun merge(other: TimeWindow): TimeWindow =
        TimeWindow(
            start = if (start.isBefore(other.start)) start else other.start,
            end = if (end.isAfter(other.end)) end else other.end,
        )

    public companion object {
        /**
         * Creates a [TimeWindow] beginning at [start] with the given [duration].
         *
         * @param start the beginning instant.
         * @param duration length of the window.
         */
        public fun of(
            start: Instant,
            duration: Duration,
        ): TimeWindow = TimeWindow(start, start.plus(duration))
    }
}
