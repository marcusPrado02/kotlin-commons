package com.marcusprado02.commons.kernel.errors

public fun Problem.withContext(
    key: String,
    value: Any,
): Problem = copy(meta = meta + (key to value))
