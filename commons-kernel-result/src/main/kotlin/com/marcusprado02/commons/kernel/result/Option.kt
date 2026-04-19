package com.marcusprado02.commons.kernel.result

@Suppress("TooManyFunctions")
public sealed class Option<out T> {
    public data class Some<out T>(
        public val value: T,
    ) : Option<T>()

    public data object None : Option<Nothing>()

    public fun isSome(): Boolean = this is Some

    public fun isNone(): Boolean = this is None

    public fun getOrNull(): T? = (this as? Some)?.value

    public fun getOrElse(default: @UnsafeVariance T): T = if (this is Some) value else default

    public fun getOrElse(default: () -> @UnsafeVariance T): T = if (this is Some) value else default()

    public fun <R> map(transform: (T) -> R): Option<R> =
        when (this) {
            is Some -> Some(transform(value))
            is None -> None
        }

    public fun <R> flatMap(transform: (T) -> Option<R>): Option<R> =
        when (this) {
            is Some -> transform(value)
            is None -> None
        }

    public fun filter(predicate: (T) -> Boolean): Option<T> =
        when (this) {
            is Some -> if (predicate(value)) this else None
            is None -> None
        }

    public fun ifSome(action: (T) -> Unit): Option<T> {
        if (this is Some) action(value)
        return this
    }

    public fun orElse(other: Option<@UnsafeVariance T>): Option<T> = if (this is Some) this else other

    public fun toResult(problem: com.marcusprado02.commons.kernel.errors.Problem): Result<T> =
        when (this) {
            is Some -> Result.ok(value)
            is None -> Result.fail(problem)
        }

    public companion object {
        public fun <T> some(value: T): Option<T> = Some(value)

        public fun <T> none(): Option<T> = None

        public fun <T> of(value: T?): Option<T> = if (value != null) Some(value) else None
    }
}

public fun <T> T?.toOption(): Option<T> = Option.of(this)
