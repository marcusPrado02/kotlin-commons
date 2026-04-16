package com.marcusprado02.commons.kernel.core

public fun requireNotBlank(value: String, lazyMessage: () -> String = { "Value must not be blank" }): String {
    require(value.isNotBlank(), lazyMessage)
    return value
}

public fun requirePositive(value: Int, lazyMessage: () -> String = { "Value must be positive, was $value" }): Int {
    require(value > 0, lazyMessage)
    return value
}

public fun requirePositive(value: Long, lazyMessage: () -> String = { "Value must be positive, was $value" }): Long {
    require(value > 0L, lazyMessage)
    return value
}
