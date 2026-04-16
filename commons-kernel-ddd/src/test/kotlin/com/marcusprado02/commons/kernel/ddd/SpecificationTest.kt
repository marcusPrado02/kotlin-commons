package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.specification.Specification
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val isEven = Specification<Int> { it % 2 == 0 }
private val isPositive = Specification<Int> { it > 0 }

class SpecificationTest : FunSpec({
    test("isSatisfiedBy evaluates predicate") {
        isEven.isSatisfiedBy(4) shouldBe true
        isEven.isSatisfiedBy(3) shouldBe false
    }

    test("and combines two specifications") {
        val isEvenAndPositive = isEven and isPositive
        isEvenAndPositive.isSatisfiedBy(4) shouldBe true
        isEvenAndPositive.isSatisfiedBy(-2) shouldBe false
        isEvenAndPositive.isSatisfiedBy(3) shouldBe false
    }

    test("or selects either specification") {
        val isEvenOrPositive = isEven or isPositive
        isEvenOrPositive.isSatisfiedBy(3) shouldBe true
        isEvenOrPositive.isSatisfiedBy(-2) shouldBe true
        isEvenOrPositive.isSatisfiedBy(-3) shouldBe false
    }

    test("not negates specification") {
        val isOdd = !isEven
        isOdd.isSatisfiedBy(3) shouldBe true
        isOdd.isSatisfiedBy(4) shouldBe false
    }
})
