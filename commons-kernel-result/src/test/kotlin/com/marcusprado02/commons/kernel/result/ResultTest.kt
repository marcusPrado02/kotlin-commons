package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class ResultTest :
    FunSpec({
        val problem = Problems.notFound(ErrorCode("NOT_FOUND"), "not found")

        test("ok is ok, not fail") {
            val r = Result.ok("hello")
            r.isOk() shouldBe true
            r.isFail() shouldBe false
            r.getOrNull() shouldBe "hello"
            r.problemOrNull().shouldBeNull()
        }

        test("fail is fail, not ok") {
            val r = Result.fail<String>(problem)
            r.isOk() shouldBe false
            r.isFail() shouldBe true
            r.getOrNull().shouldBeNull()
            r.problemOrNull() shouldBe problem
        }

        test("map transforms ok value") {
            Result.ok("hello").map { it.uppercase() } shouldBe Result.ok("HELLO")
        }

        test("map preserves fail") {
            Result.fail<String>(problem).map { it.uppercase() } shouldBe Result.fail(problem)
        }

        test("flatMap chains ok results") {
            Result.ok(2).flatMap { Result.ok(it * 3) } shouldBe Result.ok(6)
        }

        test("flatMap short-circuits on fail") {
            Result.fail<Int>(problem).flatMap { Result.ok(it * 3) } shouldBe Result.fail(problem)
        }

        test("getOrElse returns value for ok") {
            Result.ok("value").getOrElse("default") shouldBe "value"
        }

        test("getOrElse returns default for fail") {
            Result.fail<String>(problem).getOrElse("default") shouldBe "default"
        }

        test("fold applies onOk for ok") {
            Result.ok(42).fold(onFail = { -1 }, onOk = { it * 2 }) shouldBe 84
        }

        test("fold applies onFail for fail") {
            Result.fail<Int>(problem).fold(onFail = { -1 }, onOk = { it * 2 }) shouldBe -1
        }

        test("peek executes side effect for ok") {
            var called = false
            Result.ok("x").peek { called = true }
            called shouldBe true
        }

        test("peekError executes side effect for fail") {
            var called = false
            Result.fail<String>(problem).peekError { called = true }
            called shouldBe true
        }

        test("mapAsync transforms ok value in suspend context") {
            runTest {
                val r = Result.ok(10).mapAsync { it * 2 }
                r shouldBe Result.ok(20)
            }
        }

        test("mapAsync preserves fail") {
            runTest {
                val r = Result.fail<Int>(problem).mapAsync { it * 2 }
                r shouldBe Result.fail(problem)
            }
        }

        test("flatMapAsync chains ok results") {
            runTest {
                val r = Result.ok(5).flatMapAsync { Result.ok(it * 3) }
                r shouldBe Result.ok(15)
            }
        }

        test("flatMapAsync short-circuits on fail") {
            runTest {
                val r = Result.fail<Int>(problem).flatMapAsync { Result.ok(it * 3) }
                r shouldBe Result.fail(problem)
            }
        }

        test("mapError transforms problem in fail") {
            val newCode = ErrorCode("NEW_CODE")
            val r = Result.fail<String>(problem).mapError { it.copy(code = newCode) }
            r.problemOrNull()?.code shouldBe newCode
        }

        test("recoverWith converts fail to ok via Result") {
            val r = Result.fail<String>(problem).recoverWith { Result.ok("recovered") }
            r shouldBe Result.ok("recovered")
        }

        test("recover converts fail to ok") {
            val r = Result.fail<String>(problem).recover { "recovered" }
            r shouldBe Result.ok("recovered")
        }

        // T-25: zip
        test("zip combines two ok results into a pair") {
            Result.ok(1).zip(Result.ok("a")) shouldBe Result.ok(1 to "a")
        }

        test("zip returns first fail when first is fail") {
            Result.fail<Int>(problem).zip(Result.ok("a")) shouldBe Result.fail(problem)
        }

        test("zip returns second fail when second is fail") {
            Result.ok(1).zip(Result.fail<String>(problem)) shouldBe Result.fail(problem)
        }

        // T-26: zipWith
        test("zipWith combines two ok results with transform") {
            Result.ok(3).zipWith(Result.ok(4)) { a, b -> a * b } shouldBe Result.ok(12)
        }

        test("zipWith short-circuits on fail") {
            Result.fail<Int>(problem).zipWith(Result.ok(4)) { a, b -> a * b } shouldBe Result.fail(problem)
        }

        // T-27: toEither (interop)
        test("toEither converts ok to Right") {
            Result.ok(42).toEither() shouldBe Either.right(42)
        }

        test("toEither converts fail to Left") {
            Result.fail<Int>(problem).toEither() shouldBe Either.left(problem)
        }

        test("toResult converts Right to ok") {
            Either.right(42).toResult() shouldBe Result.ok(42)
        }

        test("toResult converts Left to fail") {
            Either.left(problem).toResult() shouldBe Result.fail(problem)
        }

        // T-30: sequence
        test("sequence returns ok list when all results are ok") {
            Result.sequence(listOf(Result.ok(1), Result.ok(2), Result.ok(3))) shouldBe Result.ok(listOf(1, 2, 3))
        }

        test("sequence short-circuits on first fail") {
            val result = Result.sequence(listOf(Result.ok(1), Result.fail(problem), Result.ok(3)))
            result shouldBe Result.fail(problem)
        }

        test("sequence with empty list returns ok of empty list") {
            Result.sequence(emptyList<Result<Int>>()) shouldBe Result.ok(emptyList())
        }
    })
