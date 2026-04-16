package com.marcusprado02.commons.kernel.core

import java.math.BigDecimal
import java.math.RoundingMode

public fun Double.roundTo(scale: Int, mode: RoundingMode = RoundingMode.HALF_UP): Double =
    BigDecimal.valueOf(this).setScale(scale, mode).toDouble()

public fun Int.isPositive(): Boolean = this > 0
public fun Long.isPositive(): Boolean = this > 0L
public fun Double.isPositive(): Boolean = this > 0.0

public fun Int?.orZero(): Int = this ?: 0
public fun Long?.orZero(): Long = this ?: 0L
public fun Double?.orZero(): Double = this ?: 0.0
