package com.marcusprado02.commons.adapters.messaging.kafka

import kotlin.math.min
import kotlin.math.pow

/**
 * Exponential back-off retry policy for Kafka consumer nack handling.
 *
 * @property initialDelayMs delay before the first retry in milliseconds.
 * @property multiplier factor by which the delay grows on each subsequent attempt.
 * @property maxDelayMs upper bound on the computed delay in milliseconds.
 */
public data class RetryPolicy(
    val initialDelayMs: Long = 100L,
    val multiplier: Double = 2.0,
    val maxDelayMs: Long = 10_000L,
) {
    /**
     * Computes the delay in milliseconds for the given [attempt] index (zero-based).
     *
     * @param attempt zero-based retry attempt number.
     * @return delay in milliseconds, capped at [maxDelayMs].
     */
    public fun delayFor(attempt: Int): Long {
        val delay = (initialDelayMs * multiplier.pow(attempt.toDouble())).toLong()
        return min(delay, maxDelayMs)
    }
}
