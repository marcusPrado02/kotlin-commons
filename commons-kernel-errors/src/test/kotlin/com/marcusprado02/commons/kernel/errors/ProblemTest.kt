package com.marcusprado02.commons.kernel.errors

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class ProblemTest :
    FunSpec({
        test("Problems.notFound creates a NOT_FOUND problem") {
            val code = ErrorCode("USER_NOT_FOUND")
            val p = Problems.notFound(code, "User not found")

            p.code shouldBe code
            p.category shouldBe ErrorCategory.NOT_FOUND
            p.severity shouldBe Severity.LOW
            p.message shouldBe "User not found"
            p.details shouldHaveSize 0
        }

        test("Problems.validation attaches details") {
            val detail = ProblemDetail(field = "email", message = "must be valid")
            val p = Problems.validation(ErrorCode("INVALID"), "Invalid data", detail)
            p.details shouldHaveSize 1
            p.details[0].field shouldBe "email"
        }

        test("ErrorCode rejects blank value") {
            shouldThrow<IllegalArgumentException> { ErrorCode("") }
            shouldThrow<IllegalArgumentException> { ErrorCode("   ") }
        }

        test("StandardErrorCodes provides NOT_FOUND") {
            StandardErrorCodes.NOT_FOUND.value shouldBe "NOT_FOUND"
        }
    })
