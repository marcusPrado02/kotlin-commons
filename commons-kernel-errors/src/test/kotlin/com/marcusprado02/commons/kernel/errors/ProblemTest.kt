package com.marcusprado02.commons.kernel.errors

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

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
                    .withContext("k2", "42")
            p.meta["k1"] shouldBe "v1"
            p.meta["k2"] shouldBe "42"
        }

        test("withContext does not mutate the original problem") {
            val original = Problems.notFound(ErrorCode("X"), "not found")
            original.withContext("k", "v")
            original.meta.containsKey("k") shouldBe false
        }

        test("Json.encodeToString produces valid JSON with code and message fields") {
            val problem = Problems.notFound(ErrorCode("USER_NOT_FOUND"), "User not found")
            val json = Json.encodeToString(problem)
            json shouldContain "USER_NOT_FOUND"
            json shouldContain "User not found"
        }

        // Problems factory methods — uncovered variants
        test("Problems.conflict creates a CONFLICT problem with MEDIUM severity") {
            val p = Problems.conflict(ErrorCode("DUPLICATE_EMAIL"), "Email already exists")
            p.category shouldBe ErrorCategory.CONFLICT
            p.severity shouldBe Severity.MEDIUM
            p.message shouldBe "Email already exists"
        }

        test("Problems.unauthorized creates an UNAUTHORIZED problem with HIGH severity") {
            val p = Problems.unauthorized(ErrorCode("TOKEN_EXPIRED"), "Token has expired")
            p.category shouldBe ErrorCategory.UNAUTHORIZED
            p.severity shouldBe Severity.HIGH
            p.message shouldBe "Token has expired"
        }

        test("Problems.forbidden creates a FORBIDDEN problem with HIGH severity") {
            val p = Problems.forbidden(ErrorCode("ACCESS_DENIED"), "Access denied")
            p.category shouldBe ErrorCategory.FORBIDDEN
            p.severity shouldBe Severity.HIGH
            p.message shouldBe "Access denied"
        }

        test("Problems.business creates a BUSINESS problem with MEDIUM severity") {
            val p = Problems.business(ErrorCode("ORDER_LIMIT"), "Order limit exceeded")
            p.category shouldBe ErrorCategory.BUSINESS
            p.severity shouldBe Severity.MEDIUM
            p.message shouldBe "Order limit exceeded"
        }

        test("ProblemDetail carries rejectedValue when provided") {
            val detail = ProblemDetail(field = "age", message = "must be positive", rejectedValue = "-1")
            detail.rejectedValue shouldBe "-1"
        }

        // InstantSerializer — serialize and deserialize
        test("InstantSerializer serializes an Instant to ISO-8601 JSON string") {
            val instant = Instant.parse("2024-06-15T10:30:00Z")
            val json = Json.encodeToString(InstantSerializer, instant)
            json shouldContain "2024-06-15T10:30:00Z"
        }

        test("InstantSerializer deserializes an ISO-8601 string back to Instant") {
            val isoString = "\"2024-06-15T10:30:00Z\""
            val instant = Json.decodeFromString(InstantSerializer, isoString)
            instant shouldBe Instant.parse("2024-06-15T10:30:00Z")
        }

        // fromException null-message fallbacks (covers the ?: branches for each when arm)
        test("fromException uses fallback message when NoSuchElementException message is null") {
            val p = Problems.fromException(NoSuchElementException(null as String?))
            p.message shouldBe "Not found"
        }

        test("fromException uses fallback message when IllegalStateException message is null") {
            val p = Problems.fromException(IllegalStateException(null as String?))
            p.message shouldBe "Business error"
        }

        test("fromException uses fallback message when generic exception message is null") {
            val p = Problems.fromException(RuntimeException(null as String?))
            p.message shouldBe "Technical error"
        }

        // DomainException subclasses — covers constructor branches (with and without cause)
        test("BusinessException carries the problem") {
            val problem = Problems.business(ErrorCode("RULE"), "rule violated")
            val ex = BusinessException(problem)
            ex.problem shouldBe problem
            ex.message shouldBe "rule violated"
        }

        test("BusinessException with cause wraps the throwable") {
            val problem = Problems.business(ErrorCode("RULE"), "rule violated")
            val cause = RuntimeException("root")
            val ex = BusinessException(problem, cause)
            ex.cause shouldBe cause
        }

        test("ValidationException carries the problem") {
            val problem = Problems.validation(ErrorCode("INVALID"), "bad input")
            val ex = ValidationException(problem)
            ex.problem shouldBe problem
        }

        test("NotFoundException carries the problem") {
            val problem = Problems.notFound(ErrorCode("MISSING"), "not found")
            val ex = NotFoundException(problem)
            ex.problem shouldBe problem
        }

        test("ConflictException carries the problem") {
            val problem = Problems.conflict(ErrorCode("DUP"), "duplicate")
            val ex = ConflictException(problem)
            ex.problem shouldBe problem
        }

        test("UnauthorizedException carries the problem") {
            val problem = Problems.unauthorized(ErrorCode("NO_TOKEN"), "unauthenticated")
            val ex = UnauthorizedException(problem)
            ex.problem shouldBe problem
        }

        test("ForbiddenException carries the problem") {
            val problem = Problems.forbidden(ErrorCode("NO_PERM"), "forbidden")
            val ex = ForbiddenException(problem)
            ex.problem shouldBe problem
        }

        test("TechnicalException carries the problem") {
            val problem = Problems.technical(ErrorCode("INFRA"), "infra error")
            val ex = TechnicalException(problem)
            ex.problem shouldBe problem
        }

        test("TechnicalException with cause wraps the throwable") {
            val problem = Problems.technical(ErrorCode("INFRA"), "infra error")
            val cause = RuntimeException("db down")
            val ex = TechnicalException(problem, cause)
            ex.cause shouldBe cause
        }
    })
