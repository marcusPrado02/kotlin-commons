package com.marcusprado02.commons.kernel.ddd.saga

import com.marcusprado02.commons.kernel.result.Result

/** A single named step within a saga. */
public interface SagaStep<S : Any> {
    public val name: String

    public suspend fun execute(state: S): Result<S>

    public suspend fun compensate(state: S): Result<Unit>
}
