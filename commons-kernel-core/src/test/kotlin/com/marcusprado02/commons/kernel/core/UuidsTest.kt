package com.marcusprado02.commons.kernel.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import java.util.UUID

class UuidsTest : FunSpec({
    test("randomUuid generates a valid UUID") {
        val uuid = randomUuid()
        uuid.shouldNotBeNull()
    }

    test("String.toUuid parses valid UUID") {
        val id = "550e8400-e29b-41d4-a716-446655440000"
        id.toUuid() shouldBe UUID.fromString(id)
    }

    test("String.toUuidOrNull returns null for invalid UUID") {
        "not-a-uuid".toUuidOrNull().shouldBeNull()
    }

    test("String.toUuidOrNull returns UUID for valid input") {
        val id = "550e8400-e29b-41d4-a716-446655440000"
        id.toUuidOrNull().shouldNotBeNull()
    }
})
