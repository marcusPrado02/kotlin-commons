package com.marcusprado02.commons.kernel.core

/**
 * Checks that [value] is not blank and returns it.
 *
 * @param value the string to validate.
 * @param lazyMessage message supplier invoked on failure.
 * @throws IllegalArgumentException if [value] is blank.
 */
public fun requireNotBlank(
    value: String,
    lazyMessage: () -> String = { "Value must not be blank" },
): String {
    require(value.isNotBlank(), lazyMessage)
    return value
}

/**
 * Checks that [value] is strictly positive and returns it.
 *
 * @param value the integer to validate.
 * @param lazyMessage message supplier invoked on failure.
 * @throws IllegalArgumentException if [value] is not positive.
 */
public fun requirePositive(
    value: Int,
    lazyMessage: () -> String = { "Value must be positive, was $value" },
): Int {
    require(value > 0, lazyMessage)
    return value
}

/**
 * Checks that [value] is strictly positive and returns it.
 *
 * @param value the long to validate.
 * @param lazyMessage message supplier invoked on failure.
 * @throws IllegalArgumentException if [value] is not positive.
 */
public fun requirePositive(
    value: Long,
    lazyMessage: () -> String = { "Value must be positive, was $value" },
): Long {
    require(value > 0L, lazyMessage)
    return value
}
