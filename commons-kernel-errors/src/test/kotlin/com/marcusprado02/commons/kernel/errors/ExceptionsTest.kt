package com.marcusprado02.commons.kernel.errors

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ExceptionsTest : FunSpec({
    test("NotFoundException carries problem") {
        val problem = Problems.notFound(StandardErrorCodes.NOT_FOUND, "not found")
        val ex = NotFoundException(problem)

        ex.problem shouldBe problem
        ex.message shouldBe "not found"
        ex.shouldBeInstanceOf<DomainException>()
    }

    test("ValidationException is a DomainException") {
        val ex = ValidationException(Problems.validation(ErrorCode("V"), "invalid"))
        ex.shouldBeInstanceOf<DomainException>()
    }

    test("TechnicalException wraps a cause") {
        val cause = RuntimeException("db down")
        val ex = TechnicalException(Problems.technical(StandardErrorCodes.TECHNICAL_ERROR, "db down"), cause)
        ex.cause shouldBe cause
    }
})
