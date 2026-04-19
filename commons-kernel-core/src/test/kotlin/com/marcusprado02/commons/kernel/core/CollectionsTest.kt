package com.marcusprado02.commons.kernel.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class CollectionsTest :
    FunSpec({
        test("secondOrNull returns second element") {
            listOf(1, 2, 3).secondOrNull() shouldBe 2
        }

        test("secondOrNull returns null for single-element list") {
            listOf(1).secondOrNull().shouldBeNull()
        }

        test("second returns second element") {
            listOf("a", "b").second() shouldBe "b"
        }

        test("second throws for empty list") {
            shouldThrow<NoSuchElementException> { emptyList<Int>().second() }
        }

        test("updated replaces element at index") {
            listOf(1, 2, 3).updated(1, 99) shouldBe listOf(1, 99, 3)
        }

        test("mergeWith combines maps with merge function") {
            val a = mapOf("x" to 1, "y" to 2)
            val b = mapOf("y" to 3, "z" to 4)
            val result = a.mergeWith(b) { v1, v2 -> v1 + v2 }
            result shouldBe mapOf("x" to 1, "y" to 5, "z" to 4)
        }

        test("splitWhen splits list at predicate elements") {
            listOf(1, 2, 3, 1, 2).splitWhen { it == 1 } shouldBe listOf(listOf(1, 2, 3), listOf(1, 2))
        }

        test("splitWhen returns single sublist when predicate never true") {
            listOf(1, 2, 3).splitWhen { it == 9 } shouldBe listOf(listOf(1, 2, 3))
        }

        test("splitWhen returns list of empty list for empty input") {
            emptyList<Int>().splitWhen { true } shouldBe listOf(emptyList())
        }

        test("splitWhen splits at every element when predicate always true") {
            listOf(1, 2, 3).splitWhen { true } shouldBe listOf(listOf(1), listOf(2), listOf(3))
        }
    })
