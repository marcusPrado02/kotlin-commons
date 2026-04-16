package com.marcusprado02.commons.kernel.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PreconditionsTest : FunSpec({
    test("requireNotBlank returns value for non-blank string") {
        requireNotBlank("hello") shouldBe "hello"
    }

    test("requireNotBlank throws for blank string") {
        shouldThrow<IllegalArgumentException> { requireNotBlank("  ") }
    }

    test("requirePositive returns value for positive int") {
        requirePositive(5) shouldBe 5
    }

    test("requirePositive throws for zero") {
        shouldThrow<IllegalArgumentException> { requirePositive(0) }
    }

    test("requirePositive throws for negative int") {
        shouldThrow<IllegalArgumentException> { requirePositive(-1) }
    }

    test("requirePositive works for Long - positive") {
        requirePositive(10L) shouldBe 10L
    }

    test("requirePositive throws for zero Long") {
        shouldThrow<IllegalArgumentException> { requirePositive(0L) }
    }
})
