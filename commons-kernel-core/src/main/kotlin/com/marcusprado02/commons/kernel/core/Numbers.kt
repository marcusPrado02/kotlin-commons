package com.marcusprado02.commons.kernel.core

import java.math.BigDecimal
import java.math.RoundingMode

public fun Double.roundTo(
    scale: Int,
    mode: RoundingMode = RoundingMode.HALF_UP,
): Double = BigDecimal.valueOf(this).setScale(scale, mode).toDouble()

public fun Int.isPositive(): Boolean = this > 0

public fun Long.isPositive(): Boolean = this > 0L

public fun Double.isPositive(): Boolean = this > 0.0

public fun Int?.orZero(): Int = this ?: 0

public fun Long?.orZero(): Long = this ?: 0L

public fun Double?.orZero(): Double = this ?: 0.0

private const val PERCENTAGE_MIN = 0.0
private const val PERCENTAGE_MAX = 100.0

public fun Int.clamp(
    min: Int,
    max: Int,
): Int {
    require(min <= max) { "min must be <= max" }
    return this.coerceIn(min, max)
}

public fun Long.clamp(
    min: Long,
    max: Long,
): Long {
    require(min <= max) { "min must be <= max" }
    return this.coerceIn(min, max)
}

public fun Double.clamp(
    min: Double,
    max: Double,
): Double {
    require(min <= max) { "min must be <= max" }
    return this.coerceIn(min, max)
}

public fun percentage(
    part: Number,
    total: Number,
): Double {
    val totalDouble = total.toDouble()
    if (totalDouble == PERCENTAGE_MIN) return PERCENTAGE_MIN
    return (part.toDouble() / totalDouble * PERCENTAGE_MAX).clamp(PERCENTAGE_MIN, PERCENTAGE_MAX)
}
