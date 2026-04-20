package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

@Serializable
public data class ProblemDetail(
    val field: String,
    val message: String,
    val rejectedValue: String? = null,
)
