package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PersistenceException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.util.UUID

class JpaRepositoryAdapterOptimisticLockTest :
    FunSpec({
        val mockJpa = mockk<JpaRepository<String, UUID>>()
        val adapter =
            object : JpaRepositoryAdapter<String, UUID>(mockJpa) {}

        test("save wraps ObjectOptimisticLockingFailureException as PersistenceException") {
            every { mockJpa.save(any()) } throws
                ObjectOptimisticLockingFailureException(String::class.java, UUID.randomUUID())

            val ex = shouldThrow<PersistenceException> { adapter.save("entity") }

            ex.message shouldContain "Optimistic locking failure"
            generateSequence(ex.cause) { it.cause }.any { it is ObjectOptimisticLockingFailureException } shouldBe true
        }

        test("saveAll wraps ObjectOptimisticLockingFailureException as PersistenceException") {
            every { mockJpa.saveAll(any<Iterable<String>>()) } throws
                ObjectOptimisticLockingFailureException(String::class.java, UUID.randomUUID())

            val ex = shouldThrow<PersistenceException> { adapter.saveAll(listOf("entity")) }

            ex.message shouldContain "Optimistic locking failure"
            generateSequence(ex.cause) { it.cause }.any { it is ObjectOptimisticLockingFailureException } shouldBe true
        }

        test("save wraps plain DataAccessException as PersistenceException with generic message") {
            every { mockJpa.save(any()) } throws
                org.springframework.dao.DataIntegrityViolationException("constraint")

            val ex = shouldThrow<PersistenceException> { adapter.save("entity") }

            ex.message shouldBe "save failed"
        }
    })
