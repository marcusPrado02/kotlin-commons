package com.marcusprado02.commons.kernel.ddd.valueobject

import com.marcusprado02.commons.kernel.result.Result

/**
 * Marker interface for all Value Objects in the domain model.
 *
 * Implementors may override [validate] to express invariants as a [Result], allowing callers
 * to handle validation failures without exceptions.
 */
public interface ValueObject {
    /**
     * Validates the invariants of this value object.
     *
     * The default implementation always returns [Result.success]. Override to express
     * domain-specific constraints.
     *
     * @return [Result.Ok] if all invariants hold, or a [Result.Fail] carrying a
     * [com.marcusprado02.commons.kernel.errors.Problem] describing the violation.
     */
    public fun validate(): Result<Unit> = Result.ok(Unit)
}
