package com.marcusprado02.commons.kernel.time

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Duration
import java.time.Instant

class TimeWindowTest :
    FunSpec({
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val end = Instant.parse("2026-01-31T00:00:00Z")

        test("contains returns true for instant within window") {
            val window = TimeWindow(start, end)
            window.contains(Instant.parse("2026-01-15T00:00:00Z")) shouldBe true
        }

        test("contains returns false for instant outside window") {
            val window = TimeWindow(start, end)
            window.contains(Instant.parse("2026-02-01T00:00:00Z")) shouldBe false
        }

        test("overlaps detects overlapping windows") {
            val w1 = TimeWindow(start, end)
            val w2 =
                TimeWindow(
                    Instant.parse("2026-01-15T00:00:00Z"),
                    Instant.parse("2026-02-15T00:00:00Z"),
                )
            w1.overlaps(w2) shouldBe true
        }

        test("overlaps returns false for non-overlapping windows") {
            val w1 = TimeWindow(start, end)
            val w2 =
                TimeWindow(
                    Instant.parse("2026-02-01T00:00:00Z"),
                    Instant.parse("2026-02-28T00:00:00Z"),
                )
            w1.overlaps(w2) shouldBe false
        }

        test("of factory creates window from start and duration") {
            val window = TimeWindow.of(start, Duration.ofDays(30))
            window.start shouldBe start
            window.end shouldBe start.plusSeconds(30L * 24 * 60 * 60)
        }

        test("constructor throws if end is before start") {
            shouldThrow<IllegalArgumentException> { TimeWindow(end, start) }
        }

        test("FixedClockProvider returns fixed time") {
            val fixed = FixedClockProvider(start)
            fixed.now() shouldBe start
        }

        test("SystemClockProvider returns non-null instant") {
            val clock = SystemClockProvider.clock()
            SystemClockProvider.now() shouldNotBe null
            clock shouldNotBe null
        }

        test("merge of non-overlapping windows takes earliest start and latest end") {
            val w1 = TimeWindow(start, end)
            val w2 =
                TimeWindow(
                    Instant.parse("2026-02-01T00:00:00Z"),
                    Instant.parse("2026-02-28T00:00:00Z"),
                )
            val merged = w1.merge(w2)
            merged.start shouldBe start
            merged.end shouldBe Instant.parse("2026-02-28T00:00:00Z")
        }

        test("merge of overlapping windows") {
            val w1 = TimeWindow(start, end)
            val w2 =
                TimeWindow(
                    Instant.parse("2026-01-15T00:00:00Z"),
                    Instant.parse("2026-02-15T00:00:00Z"),
                )
            val merged = w1.merge(w2)
            merged.start shouldBe start
            merged.end shouldBe Instant.parse("2026-02-15T00:00:00Z")
        }

        test("merge with itself returns equal window") {
            val w1 = TimeWindow(start, end)
            val merged = w1.merge(w1)
            merged shouldBe w1
        }

        test("advance by positive duration returns clock further in time") {
            val fixed = FixedClockProvider(start)
            val advanced = fixed.advance(Duration.ofDays(5))
            advanced.now() shouldBe start.plusSeconds(5L * 24 * 60 * 60)
        }

        test("advance by zero duration returns clock at same instant") {
            val fixed = FixedClockProvider(start)
            val advanced = fixed.advance(Duration.ZERO)
            advanced.now() shouldBe start
        }

        test("advance is immutable (original provider unchanged)") {
            val fixed = FixedClockProvider(start)
            val advanced = fixed.advance(Duration.ofDays(10))
            fixed.now() shouldBe start
            advanced.now() shouldNotBe start
        }
    })
