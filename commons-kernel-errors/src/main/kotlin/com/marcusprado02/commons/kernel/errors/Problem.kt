package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
public data class Problem(
    val code: ErrorCode,
    val category: ErrorCategory,
    val severity: Severity,
    val message: String,
    val details: List<ProblemDetail> = emptyList(),
    val meta: Map<String, String> = emptyMap(),
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant = Instant.now(),
)
