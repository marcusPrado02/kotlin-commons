package com.marcusprado02.commons.kernel.errors

import java.time.Instant

public data class Problem(
    val code: ErrorCode,
    val category: ErrorCategory,
    val severity: Severity,
    val message: String,
    val details: List<ProblemDetail> = emptyList(),
    val meta: Map<String, Any> = emptyMap(),
    val timestamp: Instant = Instant.now(),
)
