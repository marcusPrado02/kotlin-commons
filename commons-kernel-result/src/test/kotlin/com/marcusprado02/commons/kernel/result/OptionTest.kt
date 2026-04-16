package com.marcusprado02.commons.kernel.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class OptionTest : FunSpec({
    test("Some isSome, not isNone") {
        val o = Option.some("value")
        o.isSome() shouldBe true
        o.isNone() shouldBe false
        o.getOrNull() shouldBe "value"
    }

    test("None isNone, not isSome") {
        val o = Option.none<String>()
        o.isNone() shouldBe true
        o.isSome() shouldBe false
        o.getOrNull().shouldBeNull()
    }

    test("Option.of returns Some for non-null") {
        Option.of("hello") shouldBe Option.some("hello")
    }

    test("Option.of returns None for null") {
        Option.of<String>(null) shouldBe Option.none()
    }

    test("toOption extension converts nullable") {
        val s: String? = "hi"
        s.toOption() shouldBe Option.some("hi")
        val n: String? = null
        n.toOption() shouldBe Option.none()
    }

    test("map transforms Some value") {
        Option.some(5).map { it * 2 } shouldBe Option.some(10)
    }

    test("filter keeps Some when predicate is true") {
        Option.some(4).filter { it % 2 == 0 } shouldBe Option.some(4)
    }

    test("filter returns None when predicate is false") {
        Option.some(3).filter { it % 2 == 0 } shouldBe Option.none()
    }

    test("flatMap chains some values") {
        Option.some(3).flatMap { Option.some(it * 2) } shouldBe Option.some(6)
    }

    test("flatMap returns none when some maps to none") {
        Option.some(3).flatMap { Option.none<Int>() } shouldBe Option.none()
    }

    test("ifSome executes action for some") {
        var called = false
        Option.some("x").ifSome { called = true }
        called shouldBe true
    }

    test("ifSome skips action for none") {
        var called = false
        Option.none<String>().ifSome { called = true }
        called shouldBe false
    }

    test("getOrElse with lambda returns default for none") {
        Option.none<String>().getOrElse { "lazy-default" } shouldBe "lazy-default"
    }
})
