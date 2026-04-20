package com.marcusprado02.commons.kernel.result

/**
 * Represents an optional value: either [Some] containing a value, or [None].
 * Prefer [Option] over nullable types in domain APIs where the absence is meaningful.
 *
 * @param T the type of the contained value.
 */
@Suppress("TooManyFunctions")
public sealed class Option<out T> {
    /** The present variant wrapping a non-null [value]. */
    public data class Some<out T>(
        public val value: T,
    ) : Option<T>()

    /** The absent variant. */
    public data object None : Option<Nothing>()

    /** Returns `true` if this is [Some]. */
    public fun isSome(): Boolean = this is Some

    /** Returns `true` if this is [None]. */
    public fun isNone(): Boolean = this is None

    /** Returns the contained value, or `null` if [None]. */
    public fun getOrNull(): T? = (this as? Some)?.value

    /** Returns the contained value, or [default] if [None]. */
    public fun getOrElse(default: @UnsafeVariance T): T = if (this is Some) value else default

    /** Returns the contained value, or the result of [default] if [None]. */
    public fun getOrElse(default: () -> @UnsafeVariance T): T = if (this is Some) value else default()

    /**
     * Transforms the contained value with [transform], or returns [None] if absent.
     *
     * @param transform function applied to the value when [Some].
     */
    public fun <R> map(transform: (T) -> R): Option<R> =
        when (this) {
            is Some -> Some(transform(value))
            is None -> None
        }

    /**
     * Chains another [Option]-returning operation on the contained value.
     *
     * @param transform function applied to the value when [Some].
     */
    public fun <R> flatMap(transform: (T) -> Option<R>): Option<R> =
        when (this) {
            is Some -> transform(value)
            is None -> None
        }

    /**
     * Returns [None] if the contained value does not satisfy [predicate].
     *
     * @param predicate condition the value must satisfy to remain [Some].
     */
    public fun filter(predicate: (T) -> Boolean): Option<T> =
        when (this) {
            is Some -> if (predicate(value)) this else None
            is None -> None
        }

    /**
     * Executes [action] with the contained value when [Some], then returns `this` unchanged.
     *
     * @param action side-effect applied to the value.
     */
    public fun ifSome(action: (T) -> Unit): Option<T> {
        if (this is Some) action(value)
        return this
    }

    /** Returns `this` if [Some], or [other] if [None]. */
    public fun orElse(other: Option<@UnsafeVariance T>): Option<T> = if (this is Some) this else other

    /**
     * Converts to a [Result]: [Some] becomes [Result.ok], [None] becomes [Result.fail] with [problem].
     *
     * @param problem the [Problem] used when this option is [None].
     */
    public fun toResult(problem: com.marcusprado02.commons.kernel.errors.Problem): Result<T> =
        when (this) {
            is Some -> Result.ok(value)
            is None -> Result.fail(problem)
        }

    public companion object {
        /** Wraps [value] in [Some]. */
        public fun <T> some(value: T): Option<T> = Some(value)

        /** Returns the [None] singleton typed as [Option]`<T>`. */
        public fun <T> none(): Option<T> = None

        /** Returns [Some] if [value] is non-null, otherwise [None]. */
        public fun <T> of(value: T?): Option<T> = if (value != null) Some(value) else None
    }
}

/** Converts a nullable value to [Option]: non-null becomes [Option.Some], null becomes [Option.None]. */
public fun <T> T?.toOption(): Option<T> = Option.of(this)
