package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

/**
 * Field-level detail attached to a [Problem], typically produced during input validation.
 *
 * @property field name of the field that failed validation (e.g. `"email"`).
 * @property message human-readable explanation of why the field was rejected.
 * @property rejectedValue string representation of the value that was rejected, if available.
 */
@Serializable
public data class ProblemDetail(
    val field: String,
    val message: String,
    val rejectedValue: String? = null,
)
