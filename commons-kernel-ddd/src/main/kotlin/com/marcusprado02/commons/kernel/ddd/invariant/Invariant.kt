package com.marcusprado02.commons.kernel.ddd.invariant

import com.marcusprado02.commons.kernel.errors.Problem
import com.marcusprado02.commons.kernel.errors.ValidationException

public object Invariant {
    public fun check(
        condition: Boolean,
        problem: () -> Problem,
    ) {
        if (!condition) throw ValidationException(problem())
    }
}
