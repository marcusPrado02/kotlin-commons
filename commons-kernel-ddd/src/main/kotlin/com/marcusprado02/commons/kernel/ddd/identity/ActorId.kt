package com.marcusprado02.commons.kernel.ddd.identity

@JvmInline
public value class ActorId(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "ActorId must not be blank" }
    }

    override fun toString(): String = value
}
