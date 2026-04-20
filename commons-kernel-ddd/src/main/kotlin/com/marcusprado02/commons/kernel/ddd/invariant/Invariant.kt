package com.marcusprado02.commons.kernel.ddd.invariant

import com.marcusprado02.commons.kernel.errors.Problem
import com.marcusprado02.commons.kernel.errors.ValidationException

/** Utility for enforcing domain invariants by throwing [ValidationException] on violation. */
public object Invariant {
    /**
     * Throws [ValidationException] with the [Problem] produced by [problem] if [condition] is false.
     *
     * @param condition the invariant that must hold.
     * @param problem factory for the [Problem] describing the violation.
     * @throws com.marcusprado02.commons.kernel.errors.ValidationException if [condition] is false.
     */
    public fun check(
        condition: Boolean,
        problem: () -> Problem,
    ) {
        if (!condition) throw ValidationException(problem())
    }
}
