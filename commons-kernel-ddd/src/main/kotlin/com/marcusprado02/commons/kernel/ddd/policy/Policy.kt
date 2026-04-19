package com.marcusprado02.commons.kernel.ddd.policy

import com.marcusprado02.commons.kernel.ddd.event.DomainEvent

/**
 * Reacts to a [DomainEvent] of type [E].
 * Policies encapsulate the "when X happens, then do Y" rules.
 */
public interface Policy<E : DomainEvent> {
    public suspend fun handle(event: E)
}
