package com.marcusprado02.commons.kernel.ddd.specification

/**
 * Specification pattern for composable domain rules.
 *
 * Specifications can be combined with [and], [or], and [not] to build complex predicates
 * without polluting domain objects with query logic.
 *
 * @param T the type of object being evaluated.
 */
public interface Specification<T> {
    /**
     * Returns `true` if [candidate] satisfies this specification.
     *
     * @param candidate the object to evaluate.
     */
    public fun isSatisfiedBy(candidate: T): Boolean

    /**
     * Returns a new specification that is satisfied only when both this and [other] are satisfied.
     *
     * @param other the specification to combine with.
     */
    public infix fun and(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                this@Specification.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate)
        }

    /**
     * Returns a new specification that is satisfied when either this or [other] is satisfied.
     *
     * @param other the specification to combine with.
     */
    public infix fun or(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                this@Specification.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate)
        }

    /** Returns a new specification that is satisfied when this one is not. */
    public operator fun not(): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean = !this@Specification.isSatisfiedBy(candidate)
        }
}

/**
 * Creates a [Specification] from a plain [predicate] function.
 *
 * @param T the type of object being evaluated.
 * @param predicate the predicate used to evaluate candidates.
 */
public fun <T> Specification(predicate: (T) -> Boolean): Specification<T> =
    object : Specification<T> {
        override fun isSatisfiedBy(candidate: T): Boolean = predicate(candidate)
    }
