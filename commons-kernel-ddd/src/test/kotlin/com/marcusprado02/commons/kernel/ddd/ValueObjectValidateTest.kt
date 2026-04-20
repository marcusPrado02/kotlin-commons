package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.valueobject.ValueObject
import com.marcusprado02.commons.kernel.errors.ErrorCategory
import com.marcusprado02.commons.kernel.errors.Problem
import com.marcusprado02.commons.kernel.errors.Severity
import com.marcusprado02.commons.kernel.errors.StandardErrorCodes
import com.marcusprado02.commons.kernel.result.Result
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private data class PositiveAmount(val amount: Double) : ValueObject {
    override fun validate(): Result<Unit> =
        if (amount > 0) {
            Result.ok(Unit)
        } else {
            Result.fail(
                Problem(
                    code = StandardErrorCodes.VALIDATION_ERROR,
                    category = ErrorCategory.VALIDATION,
                    severity = Severity.HIGH,
                    message = "Amount must be positive, was $amount",
                ),
            )
        }
}

private data class SimpleTag(val value: String) : ValueObject

class ValueObjectValidateTest :
    FunSpec({
        test("default validate returns ok") {
            val tag = SimpleTag("kotlin")
            tag.validate().shouldBeInstanceOf<Result.Ok<Unit>>()
        }

        test("custom validate returns ok when invariant holds") {
            val amount = PositiveAmount(10.0)
            amount.validate().shouldBeInstanceOf<Result.Ok<Unit>>()
        }

        test("custom validate returns fail when invariant is violated") {
            val amount = PositiveAmount(-5.0)
            val result = amount.validate()
            result.shouldBeInstanceOf<Result.Fail>()
            (result as Result.Fail).problem.message shouldBe "Amount must be positive, was -5.0"
        }
    })
