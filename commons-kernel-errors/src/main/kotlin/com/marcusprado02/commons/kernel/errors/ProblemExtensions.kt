package com.marcusprado02.commons.kernel.errors

/**
 * Returns a copy of this [Problem] with [key]=[value] added to [Problem.meta].
 *
 * @param key metadata key.
 * @param value metadata value.
 */
public fun Problem.withContext(
    key: String,
    value: String,
): Problem = copy(meta = meta + (key to value))
