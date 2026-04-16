package com.marcusprado02.commons.kernel.errors

public sealed class DomainException(
    message: String,
    public val problem: Problem,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

public class BusinessException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class ValidationException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class NotFoundException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class ConflictException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class UnauthorizedException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class ForbiddenException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class TechnicalException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)
