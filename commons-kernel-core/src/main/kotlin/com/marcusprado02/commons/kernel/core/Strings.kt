package com.marcusprado02.commons.kernel.core

/**
 * Converts this string to a URL-friendly slug by lowercasing, stripping non-alphanumeric characters,
 * and replacing whitespace and hyphens with a single hyphen.
 */
public fun String.toSlug(): String =
    lowercase()
        .trim()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("[\\s-]+"), "-")
        .trim('-')

/**
 * Truncates this string to [maxLength] characters, appending [ellipsis] when truncation occurs.
 *
 * @param maxLength maximum number of characters in the result, including the ellipsis.
 * @param ellipsis suffix appended on truncation; defaults to `"..."`.
 * @throws IllegalArgumentException if [maxLength] is shorter than the ellipsis length.
 */
public fun String.truncate(
    maxLength: Int,
    ellipsis: String = "...",
): String {
    require(maxLength >= ellipsis.length) { "maxLength ($maxLength) must be >= ellipsis.length (${ellipsis.length})" }
    return if (length <= maxLength) this else take(maxLength - ellipsis.length) + ellipsis
}

/** Returns `null` if this string is `null` or blank, otherwise returns the string unchanged. */
public fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this

/** Capitalizes the first character of each space-separated word. */
public fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
