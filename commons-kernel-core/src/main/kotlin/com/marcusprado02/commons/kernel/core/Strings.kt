package com.marcusprado02.commons.kernel.core

public fun String.toSlug(): String =
    lowercase()
        .trim()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("[\\s-]+"), "-")
        .trim('-')

public fun String.truncate(
    maxLength: Int,
    ellipsis: String = "...",
): String {
    require(maxLength >= ellipsis.length) { "maxLength ($maxLength) must be >= ellipsis.length (${ellipsis.length})" }
    return if (length <= maxLength) this else take(maxLength - ellipsis.length) + ellipsis
}

public fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this

public fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
