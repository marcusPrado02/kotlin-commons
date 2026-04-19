package com.marcusprado02.commons.kernel.core

import io.kotest.assertions.throwables.shouldThrow
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

        test("clamp returns value when within range for Int") {
            5.clamp(1, 10) shouldBe 5
        }

        test("clamp returns min when value below range for Int") {
            (-5).clamp(0, 10) shouldBe 0
        }

        test("clamp returns max when value above range for Int") {
            20.clamp(0, 10) shouldBe 10
        }

        test("clamp works for Long") {
            15L.clamp(0L, 10L) shouldBe 10L
            (-1L).clamp(0L, 10L) shouldBe 0L
            5L.clamp(0L, 10L) shouldBe 5L
        }

        test("clamp works for Double") {
            1.5.clamp(0.0, 1.0) shouldBe 1.0
            (-0.5).clamp(0.0, 1.0) shouldBe 0.0
            0.5.clamp(0.0, 1.0) shouldBe 0.5
        }

        test("clamp throws when min greater than max") {
            shouldThrow<IllegalArgumentException> { 5.clamp(10, 1) }
        }

        test("percentage returns correct value") {
            percentage(50, 200) shouldBe 25.0
        }

        test("percentage returns 0.0 when total is zero") {
            percentage(50, 0) shouldBe 0.0
        }

        test("percentage clamps to 100.0 when part exceeds total") {
            percentage(200, 100) shouldBe 100.0
        }

        test("percentage clamps to 0.0 for negative part") {
            percentage(-10, 100) shouldBe 0.0
        }

        test("percentage works with Double inputs") {
            percentage(1.0, 4.0) shouldBe 25.0
        }
    })
