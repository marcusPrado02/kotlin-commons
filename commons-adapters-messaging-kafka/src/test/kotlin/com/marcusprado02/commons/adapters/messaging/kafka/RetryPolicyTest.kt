package com.marcusprado02.commons.adapters.messaging.kafka

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RetryPolicyTest :
    FunSpec({
        test("delayFor(0) returns initialDelayMs") {
            val policy = RetryPolicy(initialDelayMs = 100L, multiplier = 2.0, maxDelayMs = 10_000L)
            policy.delayFor(0) shouldBe 100L
        }

        test("delayFor(1) returns initialDelayMs * multiplier") {
            val policy = RetryPolicy(initialDelayMs = 100L, multiplier = 2.0, maxDelayMs = 10_000L)
            policy.delayFor(1) shouldBe 200L
        }

        test("delayFor is capped at maxDelayMs") {
            val policy = RetryPolicy(initialDelayMs = 100L, multiplier = 2.0, maxDelayMs = 500L)
            policy.delayFor(10) shouldBe 500L
        }

        test("custom multiplier works correctly") {
            val policy = RetryPolicy(initialDelayMs = 50L, multiplier = 3.0, maxDelayMs = 10_000L)
            policy.delayFor(0) shouldBe 50L
            policy.delayFor(1) shouldBe 150L
            policy.delayFor(2) shouldBe 450L
        }
    })
