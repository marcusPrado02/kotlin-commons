package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId

/** Resolves the correlation identifier for the current request or message. */
public fun interface CorrelationProvider {
    /** Returns the [CorrelationId] for the current execution context. */
    public fun currentCorrelation(): CorrelationId
}

/**
 * [CorrelationProvider] that always returns the same [correlationId].
 *
 * Useful for testing or deterministic tracing scenarios.
 */
public class FixedCorrelationProvider(
    private val correlationId: CorrelationId,
) : CorrelationProvider {
    override fun currentCorrelation(): CorrelationId = correlationId
}
