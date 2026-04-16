package com.marcusprado02.commons.kernel.errors

public object Problems {
    public fun validation(code: ErrorCode, message: String, vararg details: ProblemDetail): Problem =
        Problem(code, ErrorCategory.VALIDATION, Severity.LOW, message, details.toList())

    public fun business(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.BUSINESS, Severity.MEDIUM, message)

    public fun notFound(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.NOT_FOUND, Severity.LOW, message)

    public fun conflict(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.CONFLICT, Severity.MEDIUM, message)

    public fun unauthorized(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.UNAUTHORIZED, Severity.HIGH, message)

    public fun forbidden(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.FORBIDDEN, Severity.HIGH, message)

    public fun technical(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.TECHNICAL, Severity.HIGH, message)
}
