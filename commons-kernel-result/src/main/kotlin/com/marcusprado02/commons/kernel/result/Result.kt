package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.Problem

/**
 * Represents the outcome of an operation that can either succeed with a value ([Ok])
 * or fail with a structured [Problem] ([Fail]).
 *
 * @param T the success value type.
 */
@Suppress("TooManyFunctions")
public sealed class Result<out T> {
    /** The success variant carrying the result [value]. */
    public data class Ok<out T>(
        public val value: T,
    ) : Result<T>()

    /** The failure variant carrying the structured [problem]. */
    public data class Fail(
        public val problem: Problem,
    ) : Result<Nothing>()

    /** Returns `true` if this is [Ok]. */
    public fun isOk(): Boolean = this is Ok

    /** Returns `true` if this is [Fail]. */
    public fun isFail(): Boolean = this is Fail

    /** Returns the success value, or `null` if [Fail]. */
    public fun getOrNull(): T? = (this as? Ok)?.value

    /** Returns the [Problem], or `null` if [Ok]. */
    public fun problemOrNull(): Problem? = (this as? Fail)?.problem

    /** Returns the success value, or [default] if [Fail]. */
    public fun getOrElse(default: @UnsafeVariance T): T = if (this is Ok) value else default

    /** Returns the success value, or the result of [default] if [Fail]. */
    public fun getOrElse(default: () -> @UnsafeVariance T): T = if (this is Ok) value else default()

    /**
     * Transforms the success value with [transform], leaving [Fail] unchanged.
     *
     * @param transform function applied to the success value.
     */
    public fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Ok -> ok(transform(value))
            is Fail -> this
        }

    /**
     * Chains another [Result]-returning operation on the success value, leaving [Fail] unchanged.
     *
     * @param transform function applied to the success value.
     */
    public fun <R> flatMap(transform: (T) -> Result<R>): Result<R> =
        when (this) {
            is Ok -> transform(value)
            is Fail -> this
        }

    /**
     * Transforms the [Problem] inside [Fail] with [transform], leaving [Ok] unchanged.
     *
     * @param transform function applied to the problem.
     */
    public fun mapError(transform: (Problem) -> Problem): Result<T> =
        when (this) {
            is Ok -> this
            is Fail -> Fail(transform(problem))
        }

    /**
     * Recovers from a [Fail] by producing a success value via [transform].
     *
     * @param transform function that converts the problem into a fallback success value.
     */
    public fun recover(transform: (Problem) -> @UnsafeVariance T): Result<T> =
        when (this) {
            is Ok -> this
            is Fail -> ok(transform(problem))
        }

    /**
     * Recovers from a [Fail] by producing another [Result] via [transform].
     *
     * @param transform function that converts the problem into a fallback result.
     */
    public fun recoverWith(transform: (Problem) -> Result<@UnsafeVariance T>): Result<T> =
        when (this) {
            is Ok -> this
            is Fail -> transform(problem)
        }

    /**
     * Executes [action] with the success value when [Ok], then returns `this` unchanged.
     *
     * @param action side-effect applied to the success value.
     */
    public fun peek(action: (T) -> Unit): Result<T> {
        if (this is Ok) action(value)
        return this
    }

    /**
     * Executes [action] with the [Problem] when [Fail], then returns `this` unchanged.
     *
     * @param action side-effect applied to the problem.
     */
    public fun peekError(action: (Problem) -> Unit): Result<T> {
        if (this is Fail) action(problem)
        return this
    }

    /**
     * Collapses this result to a single value by applying [onOk] or [onFail].
     *
     * @param onFail transform applied when [Fail].
     * @param onOk transform applied when [Ok].
     */
    public fun <R> fold(
        onFail: (Problem) -> R,
        onOk: (T) -> R,
    ): R =
        when (this) {
            is Ok -> onOk(value)
            is Fail -> onFail(problem)
        }

    /**
     * Suspending variant of [map].
     *
     * @param transform suspending function applied to the success value.
     */
    public suspend fun <R> mapAsync(transform: suspend (T) -> R): Result<R> =
        when (this) {
            is Ok -> ok(transform(value))
            is Fail -> this
        }

    /**
     * Suspending variant of [flatMap].
     *
     * @param transform suspending function applied to the success value.
     */
    public suspend fun <R> flatMapAsync(transform: suspend (T) -> Result<R>): Result<R> =
        when (this) {
            is Ok -> transform(value)
            is Fail -> this
        }

    /**
     * Combines this [Ok] with another [Ok] into a [Pair], or returns the first [Fail] encountered.
     *
     * @param other the result to combine with.
     */
    public fun <R> zip(other: Result<R>): Result<Pair<T, R>> =
        when {
            this is Ok && other is Ok -> ok(value to other.value)
            this is Fail -> this
            else -> other as Fail
        }

    /**
     * Zips this result with [other] and applies [transform] to both success values.
     *
     * @param other the result to zip with.
     * @param transform function combining both success values.
     */
    public fun <R, V> zipWith(
        other: Result<R>,
        transform: (T, R) -> V,
    ): Result<V> = zip(other).map { (a, b) -> transform(a, b) }

    public companion object {
        /** Wraps [value] in [Ok]. */
        public fun <T> ok(value: T): Result<T> = Ok(value)

        /** Wraps [problem] in [Fail]. */
        public fun <T> fail(problem: Problem): Result<T> = Fail(problem)

        /**
         * Traverses a list of results, returning [Ok] with all values if every element succeeded,
         * or the first [Fail] encountered.
         *
         * @param results list of results to sequence.
         */
        public fun <T> sequence(results: List<Result<T>>): Result<List<T>> {
            val values = mutableListOf<T>()
            for (r in results) {
                when (r) {
                    is Ok -> values += r.value
                    is Fail -> return r
                }
            }
            return ok(values)
        }
    }
}
