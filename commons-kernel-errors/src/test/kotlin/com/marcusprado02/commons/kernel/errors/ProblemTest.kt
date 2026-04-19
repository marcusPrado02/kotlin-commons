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

        // Problems.fromException
        test("fromException maps IllegalArgumentException to VALIDATION category") {
            val p = Problems.fromException(IllegalArgumentException("bad arg"))
            p.category shouldBe ErrorCategory.VALIDATION
        }

        test("fromException maps NoSuchElementException to NOT_FOUND category") {
            val p = Problems.fromException(NoSuchElementException("missing"))
            p.category shouldBe ErrorCategory.NOT_FOUND
        }

        test("fromException maps IllegalStateException to BUSINESS category") {
            val p = Problems.fromException(IllegalStateException("bad state"))
            p.category shouldBe ErrorCategory.BUSINESS
        }

        test("fromException maps generic RuntimeException to TECHNICAL category") {
            val p = Problems.fromException(RuntimeException("boom"))
            p.category shouldBe ErrorCategory.TECHNICAL
        }

        test("fromException uses exception message") {
            val p = Problems.fromException(IllegalArgumentException("my message"))
            p.message shouldBe "my message"
        }

        test("fromException uses fallback message when exception message is null") {
            val p = Problems.fromException(IllegalArgumentException(null as String?))
            p.message shouldBe "Invalid argument"
        }

        // Problem.withContext
        test("withContext adds key-value to meta") {
            val p = Problems.notFound(ErrorCode("X"), "not found").withContext("requestId", "abc-123")
            p.meta["requestId"] shouldBe "abc-123"
        }

        test("withContext accumulates multiple entries") {
            val p =
                Problems
                    .notFound(ErrorCode("X"), "not found")
                    .withContext("k1", "v1")
                    .withContext("k2", 42)
            p.meta["k1"] shouldBe "v1"
            p.meta["k2"] shouldBe 42
        }

        test("withContext does not mutate the original problem") {
            val original = Problems.notFound(ErrorCode("X"), "not found")
            original.withContext("k", "v")
            original.meta.containsKey("k") shouldBe false
        }
    })
