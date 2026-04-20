package com.marcusprado02.commons.kernel.errors

/**
 * Base class for all domain-level exceptions.
 * Carries a structured [Problem] alongside the standard exception message and optional cause.
 *
 * @property problem structured description of the error.
 */
public sealed class DomainException(
    message: String,
    public val problem: Problem,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/** Thrown when a business rule is violated. */
public class BusinessException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)

/** Thrown when input data fails validation. */
public class ValidationException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)

/** Thrown when a requested resource does not exist. */
public class NotFoundException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)

/** Thrown when an operation conflicts with the current state (e.g. duplicate resource). */
public class ConflictException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)

/** Thrown when the caller is not authenticated. */
public class UnauthorizedException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)

/** Thrown when the caller is authenticated but lacks permission. */
public class ForbiddenException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)

/** Thrown for unexpected infrastructure or system-level failures. */
public class TechnicalException(
    problem: Problem,
    cause: Throwable? = null,
) : DomainException(problem.message, problem, cause)
