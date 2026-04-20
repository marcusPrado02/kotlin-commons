package com.marcusprado02.commons.kernel.ddd.entity

/**
 * Monotonically increasing version counter for optimistic-concurrency control.
 *
 * @property value the numeric version; must be non-negative.
 */
@JvmInline
public value class EntityVersion(
    public val value: Long,
) {
    init {
        require(value >= 0) { "EntityVersion must be non-negative, was $value" }
    }

    /** Returns a new [EntityVersion] incremented by one. */
    public fun increment(): EntityVersion = EntityVersion(value + 1)

    override fun toString(): String = value.toString()

    public companion object {
        /** The version assigned to a newly created entity. */
        public val INITIAL: EntityVersion = EntityVersion(0L)
    }
}
