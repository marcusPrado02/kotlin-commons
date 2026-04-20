package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.saga.SagaStep
import com.marcusprado02.commons.kernel.errors.ErrorCategory
import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import com.marcusprado02.commons.kernel.errors.Severity
import com.marcusprado02.commons.kernel.result.Result
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private data class OrderState(
    val orderId: String,
    val reserved: Boolean = false,
    val charged: Boolean = false,
)

private object ReserveInventoryStep : SagaStep<OrderState> {
    override val name: String = "reserve-inventory"

    override suspend fun execute(state: OrderState): Result<OrderState> = Result.ok(state.copy(reserved = true))

    override suspend fun compensate(state: OrderState): Result<Unit> = Result.ok(Unit)
}

private object ChargePaymentStep : SagaStep<OrderState> {
    override val name: String = "charge-payment"

    override suspend fun execute(state: OrderState): Result<OrderState> =
        Result.fail(
            Problems.technical(ErrorCode("PAYMENT_FAILED"), "Payment gateway error"),
        )

    override suspend fun compensate(state: OrderState): Result<Unit> = Result.ok(Unit)
}

class SagaTest :
    FunSpec({
        test("a concrete saga step can execute and return updated state") {
            val initial = OrderState(orderId = "order-1")
            val result = ReserveInventoryStep.execute(initial)

            result.isOk() shouldBe true
            result.getOrNull()!!.reserved shouldBe true
            result.getOrNull()!!.orderId shouldBe "order-1"
        }

        test("a saga step can compensate and return ok") {
            val state = OrderState(orderId = "order-1", reserved = true)
            val result = ReserveInventoryStep.compensate(state)

            result.isOk() shouldBe true
            result.getOrNull() shouldBe Unit
        }

        test("a failing saga step returns a Fail result") {
            val state = OrderState(orderId = "order-2")
            val result = ChargePaymentStep.execute(state)

            result.isFail() shouldBe true
            result.shouldBeInstanceOf<Result.Fail>()
            result.problemOrNull()!!.code shouldBe ErrorCode("PAYMENT_FAILED")
            result.problemOrNull()!!.category shouldBe ErrorCategory.TECHNICAL
            result.problemOrNull()!!.severity shouldBe Severity.HIGH
        }
    })
