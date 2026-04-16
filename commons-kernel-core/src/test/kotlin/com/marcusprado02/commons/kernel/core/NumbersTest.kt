package com.marcusprado02.commons.kernel.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NumbersTest :
    FunSpec({
        test("roundTo rounds double to given scale") {
            1.456.roundTo(2) shouldBe 1.46
        }

        test("isPositive returns true for positive int") {
            5.isPositive() shouldBe true
        }

        test("isPositive returns false for zero int") {
            0.isPositive() shouldBe false
        }

        test("isPositive returns false for negative int") {
            (-1).isPositive() shouldBe false
        }

        test("isPositive works for Long") {
            3L.isPositive() shouldBe true
            0L.isPositive() shouldBe false
        }

        test("isPositive works for Double") {
            1.5.isPositive() shouldBe true
            (-0.1).isPositive() shouldBe false
        }

        test("orZero returns 0 for null Int") {
            val n: Int? = null
            n.orZero() shouldBe 0
        }

        test("orZero returns value for non-null Int") {
            val n: Int? = 5
            n.orZero() shouldBe 5
        }

        test("orZero works for Long") {
            val n: Long? = null
            n.orZero() shouldBe 0L
        }

        test("orZero works for Double") {
            val n: Double? = null
            n.orZero() shouldBe 0.0
        }
    })
