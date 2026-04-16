package com.marcusprado02.commons.kernel.ddd.specification

public interface Specification<T> {
    public fun isSatisfiedBy(candidate: T): Boolean

    public infix fun and(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                this@Specification.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate)
        }

    public infix fun or(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                this@Specification.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate)
        }

    public operator fun not(): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                !this@Specification.isSatisfiedBy(candidate)
        }
}

public fun <T> Specification(predicate: (T) -> Boolean): Specification<T> =
    object : Specification<T> {
        override fun isSatisfiedBy(candidate: T): Boolean = predicate(candidate)
    }
