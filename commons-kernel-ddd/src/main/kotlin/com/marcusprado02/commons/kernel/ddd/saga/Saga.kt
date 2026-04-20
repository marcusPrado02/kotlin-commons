package com.marcusprado02.commons.kernel.ddd.saga

import com.marcusprado02.commons.kernel.result.Result

/**
 * Represents a long-running process that coordinates multiple domain operations.
 * Each saga consists of steps that can be compensated (rolled back) on failure.
 *
 * @param S the type representing the saga's state
 * @param R the result produced when the saga completes successfully
 */
public interface Saga<S : Any, R : Any> {
    /** Executes the next step given the current state. Returns updated state or a Result. */
    public suspend fun step(state: S): Result<S>

    /** Compensates (rolls back) the saga from the given state. */
    public suspend fun compensate(state: S): Result<Unit>
}
