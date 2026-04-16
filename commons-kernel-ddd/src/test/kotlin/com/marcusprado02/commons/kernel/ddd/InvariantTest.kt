package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.invariant.Invariant
import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import com.marcusprado02.commons.kernel.errors.ValidationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class InvariantTest : FunSpec({
    test("check does not throw when condition is true") {
        Invariant.check(true) { Problems.validation(ErrorCode("ERR"), "fail") }
    }

    test("check throws ValidationException when condition is false") {
        val ex = shouldThrow<ValidationException> {
            Invariant.check(false) { Problems.validation(ErrorCode("ERR"), "invariant violated") }
        }
        ex.message shouldBe "invariant violated"
    }
})
