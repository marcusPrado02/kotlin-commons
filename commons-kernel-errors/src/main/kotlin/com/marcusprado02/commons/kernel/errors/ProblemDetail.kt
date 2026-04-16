package com.marcusprado02.commons.kernel.errors

public data class ProblemDetail(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
)
