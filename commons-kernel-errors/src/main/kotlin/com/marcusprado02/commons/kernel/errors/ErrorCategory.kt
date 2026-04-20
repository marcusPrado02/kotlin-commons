package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

/** Broad classification of an error, used to map problems to HTTP status codes or UI behaviour. */
@Serializable
public enum class ErrorCategory {
    VALIDATION,
    BUSINESS,
    NOT_FOUND,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,
    TECHNICAL,
}
