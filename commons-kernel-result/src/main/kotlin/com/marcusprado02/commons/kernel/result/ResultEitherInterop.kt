package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.Problem

/** Converts a [Result] to an [Either], mapping [Result.Ok] to [Either.Right] and [Result.Fail] to [Either.Left]. */
public fun <T> Result<T>.toEither(): Either<Problem, T> =
    when (this) {
        is Result.Ok -> Either.Right(value)
        is Result.Fail -> Either.Left(problem)
    }

/** Converts an [Either]`<Problem, T>` to a [Result], mapping [Either.Right] to [Result.Ok] and [Either.Left] to [Result.Fail]. */
public fun <T> Either<Problem, T>.toResult(): Result<T> =
    when (this) {
        is Either.Right -> Result.ok(value)
        is Either.Left -> Result.fail(value)
    }
