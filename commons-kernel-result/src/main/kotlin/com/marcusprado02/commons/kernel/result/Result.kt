package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.Problem

public sealed class Result<out T> {

    public data class Ok<out T>(public val value: T) : Result<T>()
    public data class Fail(public val problem: Problem) : Result<Nothing>()

    public fun isOk(): Boolean = this is Ok
    public fun isFail(): Boolean = this is Fail

    public fun getOrNull(): T? = (this as? Ok)?.value
    public fun problemOrNull(): Problem? = (this as? Fail)?.problem

    public fun getOrElse(default: @UnsafeVariance T): T = if (this is Ok) value else default
    public fun getOrElse(default: () -> @UnsafeVariance T): T = if (this is Ok) value else default()

    public fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Ok -> ok(transform(value))
        is Fail -> this
    }

    public fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Ok -> transform(value)
        is Fail -> this
    }

    public fun mapError(transform: (Problem) -> Problem): Result<T> = when (this) {
        is Ok -> this
        is Fail -> Fail(transform(problem))
    }

    public fun recover(transform: (Problem) -> @UnsafeVariance T): Result<T> = when (this) {
        is Ok -> this
        is Fail -> ok(transform(problem))
    }

    public fun recoverWith(transform: (Problem) -> Result<@UnsafeVariance T>): Result<T> = when (this) {
        is Ok -> this
        is Fail -> transform(problem)
    }

    public fun peek(action: (T) -> Unit): Result<T> {
        if (this is Ok) action(value)
        return this
    }

    public fun peekError(action: (Problem) -> Unit): Result<T> {
        if (this is Fail) action(problem)
        return this
    }

    public fun <R> fold(onFail: (Problem) -> R, onOk: (T) -> R): R = when (this) {
        is Ok -> onOk(value)
        is Fail -> onFail(problem)
    }

    public suspend fun <R> mapAsync(transform: suspend (T) -> R): Result<R> = when (this) {
        is Ok -> ok(transform(value))
        is Fail -> this
    }

    public suspend fun <R> flatMapAsync(transform: suspend (T) -> Result<R>): Result<R> = when (this) {
        is Ok -> transform(value)
        is Fail -> this
    }

    public companion object {
        public fun <T> ok(value: T): Result<T> = Ok(value)
        public fun <T> fail(problem: Problem): Result<T> = Fail(problem)
    }
}
