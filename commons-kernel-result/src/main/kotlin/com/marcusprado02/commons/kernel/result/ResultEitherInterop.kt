package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.Problem

public fun <T> Result<T>.toEither(): Either<Problem, T> =
    when (this) {
        is Result.Ok -> Either.Right(value)
        is Result.Fail -> Either.Left(problem)
    }

public fun <T> Either<Problem, T>.toResult(): Result<T> =
    when (this) {
        is Either.Right -> Result.ok(value)
        is Either.Left -> Result.fail(value)
    }
