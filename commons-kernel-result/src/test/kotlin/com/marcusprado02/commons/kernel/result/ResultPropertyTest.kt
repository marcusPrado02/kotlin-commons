package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class ResultPropertyTest :
    FunSpec({
        val problem = Problems.notFound(ErrorCode("NOT_FOUND"), "not found")

        // Functor law: identity — map(id) == id
        test("map identity law") {
            checkAll(Arb.int()) { n ->
                Result.ok(n).map { it } shouldBe Result.ok(n)
            }
        }

        // Functor law: composition — map(f.g) == map(f).map(g)
        test("map composition law") {
            val f: (Int) -> Int = { it * 2 }
            val g: (Int) -> String = { it.toString() }
            checkAll(Arb.int()) { n ->
                Result.ok(n).map { g(f(it)) } shouldBe Result.ok(n).map(f).map(g)
            }
        }

        // Monad law: left identity — flatMap(ok(a), f) == f(a)
        test("flatMap left identity law") {
            val f: (Int) -> Result<String> = { Result.ok(it.toString()) }
            checkAll(Arb.int()) { n ->
                Result.ok(n).flatMap(f) shouldBe f(n)
            }
        }

        // Monad law: right identity — flatMap(m, ok) == m
        test("flatMap right identity law") {
            checkAll(Arb.int()) { n ->
                Result.ok(n).flatMap { Result.ok(it) } shouldBe Result.ok(n)
            }
        }

        // Monad law: associativity — flatMap(flatMap(m, f), g) == flatMap(m, x -> flatMap(f(x), g))
        test("flatMap associativity law") {
            val f: (Int) -> Result<Int> = { Result.ok(it * 2) }
            val g: (Int) -> Result<String> = { Result.ok(it.toString()) }
            checkAll(Arb.int()) { n ->
                Result.ok(n).flatMap(f).flatMap(g) shouldBe Result.ok(n).flatMap { f(it).flatMap(g) }
            }
        }

        // Fail propagation: map on Fail always returns the same Fail
        test("map on Fail is no-op") {
            checkAll(Arb.int()) { n ->
                Result.fail<Int>(problem).map { it + n } shouldBe Result.fail(problem)
            }
        }

        // Fail propagation: flatMap on Fail never calls the transform
        test("flatMap on Fail is no-op") {
            checkAll(Arb.int()) { n ->
                Result.fail<Int>(problem).flatMap { Result.ok(it + n) } shouldBe Result.fail(problem)
            }
        }

        // getOrElse: Ok returns value, Fail returns default
        test("getOrElse returns value for Ok and default for Fail") {
            checkAll(Arb.int(), Arb.int()) { value, default ->
                Result.ok(value).getOrElse(default) shouldBe value
                Result.fail<Int>(problem).getOrElse(default) shouldBe default
            }
        }

        // sequence: all Ok -> Ok of list
        test("sequence of all Ok returns Ok of collected values") {
            checkAll(Arb.int(), Arb.int(), Arb.int()) { a, b, c ->
                val results = listOf(Result.ok(a), Result.ok(b), Result.ok(c))
                Result.sequence(results) shouldBe Result.ok(listOf(a, b, c))
            }
        }

        // sequence: any Fail short-circuits
        test("sequence short-circuits on first Fail") {
            checkAll(Arb.int()) { n ->
                val results = listOf(Result.ok(n), Result.fail(problem), Result.ok(n))
                Result.sequence(results) shouldBe Result.fail(problem)
            }
        }

        // zip: two Ok values combine
        test("zip combines two Ok results") {
            checkAll(Arb.int(), Arb.string()) { n, s ->
                Result.ok(n).zip(Result.ok(s)) shouldBe Result.ok(n to s)
            }
        }

        // zip: Fail propagates
        test("zip propagates Fail from first operand") {
            checkAll(Arb.int()) { n ->
                Result.fail<Int>(problem).zip(Result.ok(n)) shouldBe Result.fail(problem)
            }
        }

        // recover: Ok stays Ok, Fail becomes Ok
        test("recover converts Fail to Ok") {
            checkAll(Arb.int()) { n ->
                Result.ok(n).recover { n + 1 } shouldBe Result.ok(n)
                Result.fail<Int>(problem).recover { n } shouldBe Result.ok(n)
            }
        }
    })
