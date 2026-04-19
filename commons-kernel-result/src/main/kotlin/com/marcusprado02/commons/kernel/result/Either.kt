package com.marcusprado02.commons.kernel.result

public sealed class Either<out L, out R> {
    public data class Left<out L>(
        public val value: L,
    ) : Either<L, Nothing>()

    public data class Right<out R>(
        public val value: R,
    ) : Either<Nothing, R>()

    public fun isLeft(): Boolean = this is Left

    public fun isRight(): Boolean = this is Right

    public fun leftOrNull(): L? = (this as? Left)?.value

    public fun rightOrNull(): R? = (this as? Right)?.value

    public fun <T> fold(
        onLeft: (L) -> T,
        onRight: (R) -> T,
    ): T =
        when (this) {
            is Left -> onLeft(value)
            is Right -> onRight(value)
        }

    public fun <T> mapRight(transform: (R) -> T): Either<L, T> =
        when (this) {
            is Left -> this
            is Right -> Right(transform(value))
        }

    public fun <T> mapLeft(transform: (L) -> T): Either<T, R> =
        when (this) {
            is Left -> Left(transform(value))
            is Right -> this
        }

    public fun <T> flatMapRight(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> =
        when (this) {
            is Left -> this
            is Right -> transform(value)
        }

    public fun swap(): Either<R, L> =
        when (this) {
            is Left -> Right(value)
            is Right -> Left(value)
        }

    public fun <T> flatMapLeft(transform: (L) -> Either<T, @UnsafeVariance R>): Either<T, R> =
        when (this) {
            is Left -> transform(value)
            is Right -> this
        }

    public companion object {
        public fun <L> left(value: L): Either<L, Nothing> = Left(value)

        public fun <R> right(value: R): Either<Nothing, R> = Right(value)
    }
}
