package com.marcusprado02.commons.kernel.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class EitherTest :
    FunSpec({
        test("Left isLeft, not isRight") {
            val e = Either.left("error")
            e.isLeft() shouldBe true
            e.isRight() shouldBe false
            e.leftOrNull() shouldBe "error"
            e.rightOrNull().shouldBeNull()
        }

        test("Right isRight, not isLeft") {
            val e = Either.right(42)
            e.isRight() shouldBe true
            e.isLeft() shouldBe false
            e.rightOrNull() shouldBe 42
            e.leftOrNull().shouldBeNull()
        }

        test("mapRight transforms right value") {
            Either.right(10).mapRight { it * 2 } shouldBe Either.right(20)
        }

        test("mapRight preserves left") {
            val e: Either<String, Int> = Either.left("err")
            e.mapRight { it * 2 } shouldBe Either.left("err")
        }

        test("fold selects correct branch") {
            Either.left("error").fold(onLeft = { "L:$it" }, onRight = { "R:$it" }) shouldBe "L:error"
            Either.right(99).fold(onLeft = { "L:$it" }, onRight = { "R:$it" }) shouldBe "R:99"
        }

        test("mapLeft transforms left value") {
            Either.left("err").mapLeft { it.uppercase() } shouldBe Either.left("ERR")
        }

        test("mapLeft preserves right") {
            val e: Either<String, Int> = Either.right(42)
            e.mapLeft { it.uppercase() } shouldBe Either.right(42)
        }

        test("flatMapRight chains right values") {
            Either.right(5).flatMapRight { Either.right(it * 2) } shouldBe Either.right(10)
        }

        test("flatMapRight preserves left on Either") {
            val e: Either<String, Int> = Either.left("err")
            e.flatMapRight { Either.right(it * 2) } shouldBe Either.left("err")
        }
    })
