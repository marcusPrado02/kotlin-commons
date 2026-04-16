package com.marcusprado02.commons.kernel.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class StringsTest :
    FunSpec({
        test("toSlug converts spaces and special chars") {
            "Hello World!".toSlug() shouldBe "hello-world"
        }

        test("truncate shortens long strings with ellipsis") {
            "Hello World".truncate(8) shouldBe "Hello..."
        }

        test("truncate returns original if within limit") {
            "Hello".truncate(10) shouldBe "Hello"
        }

        test("nullIfBlank returns null for blank string") {
            "   ".nullIfBlank().shouldBeNull()
        }

        test("nullIfBlank returns value for non-blank string") {
            "hello".nullIfBlank().shouldNotBeNull() shouldBe "hello"
        }

        test("nullIfBlank on null returns null") {
            val s: String? = null
            s.nullIfBlank().shouldBeNull()
        }

        test("capitalizeWords capitalizes each word") {
            "hello world".capitalizeWords() shouldBe "Hello World"
        }

        test("capitalizeWords single word") {
            "kotlin".capitalizeWords() shouldBe "Kotlin"
        }

        test("capitalizeWords empty string") {
            "".capitalizeWords() shouldBe ""
        }

        test("capitalizeWords already capitalized is idempotent") {
            "Hello World".capitalizeWords() shouldBe "Hello World"
        }
    })
