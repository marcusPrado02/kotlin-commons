package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class ResultTest : FunSpec({
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
})
