package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId

public fun interface CorrelationProvider {
    public fun currentCorrelation(): CorrelationId
}

public class FixedCorrelationProvider(private val correlationId: CorrelationId) : CorrelationProvider {
    override fun currentCorrelation(): CorrelationId = correlationId
}
