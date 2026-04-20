package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Structured representation of an error occurrence.
 *
 * @property message human-readable description of the error.
 * @property details field-level details, typically used for validation failures.
 * @property meta arbitrary key-value context attached to the problem (e.g. trace IDs, entity IDs).
 * @property timestamp when the problem was created.
 */
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
