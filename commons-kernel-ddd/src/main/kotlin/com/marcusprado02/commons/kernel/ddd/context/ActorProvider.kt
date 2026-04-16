package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.ActorId

public fun interface ActorProvider {
    public fun currentActor(): ActorId
}

public class FixedActorProvider(
    private val actorId: ActorId,
) : ActorProvider {
    override fun currentActor(): ActorId = actorId
}
