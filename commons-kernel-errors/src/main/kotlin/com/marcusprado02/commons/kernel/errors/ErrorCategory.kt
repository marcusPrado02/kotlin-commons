package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

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
