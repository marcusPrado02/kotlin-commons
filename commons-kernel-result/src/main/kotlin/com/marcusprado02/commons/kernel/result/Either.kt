package com.marcusprado02.commons.kernel.result

/**
 * Represents a value of one of two possible types ([Left] or [Right]).
 * By convention, [Left] holds an error or failure value and [Right] holds a success value.
 *
 * @param L the left (error) type.
 * @param R the right (success) type.
 */
public sealed class Either<out L, out R> {
    /** The left (typically error) variant. */
    public data class Left<out L>(
        public val value: L,
    ) : Either<L, Nothing>()

    /** The right (typically success) variant. */
    public data class Right<out R>(
        public val value: R,
    ) : Either<Nothing, R>()

    /** Returns `true` if this is [Left]. */
    public fun isLeft(): Boolean = this is Left

    /** Returns `true` if this is [Right]. */
    public fun isRight(): Boolean = this is Right

    /** Returns the [Left] value, or `null` if this is [Right]. */
    public fun leftOrNull(): L? = (this as? Left)?.value

    /** Returns the [Right] value, or `null` if this is [Left]. */
    public fun rightOrNull(): R? = (this as? Right)?.value

    /**
     * Applies [onLeft] if this is [Left], or [onRight] if this is [Right], returning the result.
     *
     * @param onLeft transform applied to the left value.
     * @param onRight transform applied to the right value.
     */
    public fun <T> fold(
        onLeft: (L) -> T,
        onRight: (R) -> T,
    ): T =
        when (this) {
            is Left -> onLeft(value)
            is Right -> onRight(value)
        }

    /**
     * Transforms the [Right] value with [transform], leaving [Left] unchanged.
     *
     * @param transform function applied to the right value.
     */
    public fun <T> mapRight(transform: (R) -> T): Either<L, T> =
        when (this) {
            is Left -> this
            is Right -> Right(transform(value))
        }

    /**
     * Transforms the [Left] value with [transform], leaving [Right] unchanged.
     *
     * @param transform function applied to the left value.
     */
    public fun <T> mapLeft(transform: (L) -> T): Either<T, R> =
        when (this) {
            is Left -> Left(transform(value))
            is Right -> this
        }

    /**
     * Chains another [Either]-returning operation on the [Right] value, leaving [Left] unchanged.
     *
     * @param transform function applied to the right value.
     */
    public fun <T> flatMapRight(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> =
        when (this) {
            is Left -> this
            is Right -> transform(value)
        }

    /** Swaps [Left] and [Right], returning an [Either] with the types reversed. */
    public fun swap(): Either<R, L> =
        when (this) {
            is Left -> Right(value)
            is Right -> Left(value)
        }

    /**
     * Chains another [Either]-returning operation on the [Left] value, leaving [Right] unchanged.
     *
     * @param transform function applied to the left value.
     */
    public fun <T> flatMapLeft(transform: (L) -> Either<T, @UnsafeVariance R>): Either<T, R> =
        when (this) {
            is Left -> transform(value)
            is Right -> this
        }

    public companion object {
        /** Wraps [value] in the [Left] variant. */
        public fun <L> left(value: L): Either<L, Nothing> = Left(value)

        /** Wraps [value] in the [Right] variant. */
        public fun <R> right(value: R): Either<Nothing, R> = Right(value)
    }
}
