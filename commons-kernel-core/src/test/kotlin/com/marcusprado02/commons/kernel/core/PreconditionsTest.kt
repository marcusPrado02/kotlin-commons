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
})
