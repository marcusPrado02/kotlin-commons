package com.marcusprado02.commons.kernel.errors

/** Factory object for constructing [Problem] instances with pre-set category and severity. */
public object Problems {
    /**
     * Creates a [Problem] with [ErrorCategory.VALIDATION] and [Severity.LOW].
     *
     * @param details optional field-level validation details.
     */
    public fun validation(
        code: ErrorCode,
        message: String,
        vararg details: ProblemDetail,
    ): Problem = Problem(code, ErrorCategory.VALIDATION, Severity.LOW, message, details.toList())

    /** Creates a [Problem] with [ErrorCategory.BUSINESS] and [Severity.MEDIUM]. */
    public fun business(
        code: ErrorCode,
        message: String,
    ): Problem = Problem(code, ErrorCategory.BUSINESS, Severity.MEDIUM, message)

    /** Creates a [Problem] with [ErrorCategory.NOT_FOUND] and [Severity.LOW]. */
    public fun notFound(
        code: ErrorCode,
        message: String,
    ): Problem = Problem(code, ErrorCategory.NOT_FOUND, Severity.LOW, message)

    /** Creates a [Problem] with [ErrorCategory.CONFLICT] and [Severity.MEDIUM]. */
    public fun conflict(
        code: ErrorCode,
        message: String,
    ): Problem = Problem(code, ErrorCategory.CONFLICT, Severity.MEDIUM, message)

    /** Creates a [Problem] with [ErrorCategory.UNAUTHORIZED] and [Severity.HIGH]. */
    public fun unauthorized(
        code: ErrorCode,
        message: String,
    ): Problem = Problem(code, ErrorCategory.UNAUTHORIZED, Severity.HIGH, message)

    /** Creates a [Problem] with [ErrorCategory.FORBIDDEN] and [Severity.HIGH]. */
    public fun forbidden(
        code: ErrorCode,
        message: String,
    ): Problem = Problem(code, ErrorCategory.FORBIDDEN, Severity.HIGH, message)

    /** Creates a [Problem] with [ErrorCategory.TECHNICAL] and [Severity.HIGH]. */
    public fun technical(
        code: ErrorCode,
        message: String,
    ): Problem = Problem(code, ErrorCategory.TECHNICAL, Severity.HIGH, message)

    /**
     * Converts a [Throwable] to a [Problem] by mapping well-known exception types to their
     * corresponding categories. Falls back to [ErrorCategory.TECHNICAL] for unknown types.
     *
     * @param e the exception to convert.
     */
    public fun fromException(e: Throwable): Problem =
        when (e) {
            is IllegalArgumentException ->
                validation(StandardErrorCodes.VALIDATION_ERROR, e.message ?: "Invalid argument")
            is NoSuchElementException ->
                notFound(StandardErrorCodes.NOT_FOUND, e.message ?: "Not found")
            is IllegalStateException ->
                business(StandardErrorCodes.BUSINESS_ERROR, e.message ?: "Business error")
            else ->
                technical(StandardErrorCodes.TECHNICAL_ERROR, e.message ?: "Technical error")
        }
}
