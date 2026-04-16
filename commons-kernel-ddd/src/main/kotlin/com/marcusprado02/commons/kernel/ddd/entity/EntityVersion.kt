package com.marcusprado02.commons.kernel.ddd.entity

@JvmInline
public value class EntityVersion(
    public val value: Long,
) {
    init {
        require(value >= 0) { "EntityVersion must be non-negative, was $value" }
    }

    public fun increment(): EntityVersion = EntityVersion(value + 1)

    override fun toString(): String = value.toString()

    public companion object {
        public val INITIAL: EntityVersion = EntityVersion(0L)
    }
}
