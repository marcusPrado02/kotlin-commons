package com.marcusprado02.commons.adapters.messaging.kafka

import kotlin.math.min
import kotlin.math.pow

public data class RetryPolicy(
    val initialDelayMs: Long = 100L,
    val multiplier: Double = 2.0,
    val maxDelayMs: Long = 10_000L,
) {
    public fun delayFor(attempt: Int): Long {
        val delay = (initialDelayMs * multiplier.pow(attempt.toDouble())).toLong()
        return min(delay, maxDelayMs)
    }
}
