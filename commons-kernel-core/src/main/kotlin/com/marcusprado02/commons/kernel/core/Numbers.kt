package com.marcusprado02.commons.kernel.core

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Rounds this [Double] to the given number of decimal places using [mode].
 *
 * @param scale number of decimal places.
 * @param mode rounding strategy; defaults to [RoundingMode.HALF_UP].
 */
public fun Double.roundTo(
    scale: Int,
    mode: RoundingMode = RoundingMode.HALF_UP,
): Double = BigDecimal.valueOf(this).setScale(scale, mode).toDouble()

/** Returns `true` if this value is strictly greater than zero. */
public fun Int.isPositive(): Boolean = this > 0

/** Returns `true` if this value is strictly greater than zero. */
public fun Long.isPositive(): Boolean = this > 0L

/** Returns `true` if this value is strictly greater than zero. */
public fun Double.isPositive(): Boolean = this > 0.0

/** Returns this value, or `0` if `null`. */
public fun Int?.orZero(): Int = this ?: 0

/** Returns this value, or `0` if `null`. */
public fun Long?.orZero(): Long = this ?: 0L

/** Returns this value, or `0.0` if `null`. */
public fun Double?.orZero(): Double = this ?: 0.0

private const val PERCENTAGE_MIN = 0.0
private const val PERCENTAGE_MAX = 100.0

/**
 * Constrains this value to the closed range [[min], [max]].
 *
 * @throws IllegalArgumentException if [min] > [max].
 */
public fun Int.clamp(
    min: Int,
    max: Int,
): Int {
    require(min <= max) { "min must be <= max" }
    return this.coerceIn(min, max)
}

/**
 * Constrains this value to the closed range [[min], [max]].
 *
 * @throws IllegalArgumentException if [min] > [max].
 */
public fun Long.clamp(
    min: Long,
    max: Long,
): Long {
    require(min <= max) { "min must be <= max" }
    return this.coerceIn(min, max)
}

/**
 * Constrains this value to the closed range [[min], [max]].
 *
 * @throws IllegalArgumentException if [min] > [max].
 */
public fun Double.clamp(
    min: Double,
    max: Double,
): Double {
    require(min <= max) { "min must be <= max" }
    return this.coerceIn(min, max)
}

/**
 * Computes what percentage [part] is of [total], clamped to `[0.0, 100.0]`.
 * Returns `0.0` when [total] is zero.
 *
 * @param part the partial quantity.
 * @param total the whole quantity.
 */
public fun percentage(
    part: Number,
    total: Number,
): Double {
    val totalDouble = total.toDouble()
    if (totalDouble == PERCENTAGE_MIN) return PERCENTAGE_MIN
    return (part.toDouble() / totalDouble * PERCENTAGE_MAX).clamp(PERCENTAGE_MIN, PERCENTAGE_MAX)
}
