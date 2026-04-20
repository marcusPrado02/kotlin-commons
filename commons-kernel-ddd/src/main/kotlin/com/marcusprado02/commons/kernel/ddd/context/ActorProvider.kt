package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.ActorId

/** Resolves the identity of the currently executing actor. */
public fun interface ActorProvider {
    /** Returns the [ActorId] for the current execution context. */
    public fun currentActor(): ActorId
}

/**
 * [ActorProvider] that always returns the same [actorId].
 *
 * Useful for testing or single-actor deployments.
 */
public class FixedActorProvider(
    private val actorId: ActorId,
) : ActorProvider {
    override fun currentActor(): ActorId = actorId
}
