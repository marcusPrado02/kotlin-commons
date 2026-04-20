package com.marcusprado02.commons.kernel.ddd.identity

/**
 * Identity of the actor (user or service) performing an operation.
 *
 * @property value the non-blank string identifying the actor.
 */
@JvmInline
public value class ActorId(
    public val value: String,
) {
    init {
        require(value.isNotBlank()) { "ActorId must not be blank" }
    }

    override fun toString(): String = value
}
