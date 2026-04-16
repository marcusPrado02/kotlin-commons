package com.marcusprado02.commons.ports.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull

private class InMemoryRepository<E : Any, I : Any>(
    private val getId: (E) -> I,
) : Repository<E, I> {
    private val store = mutableMapOf<I, E>()

    override suspend fun findById(id: I): E? = store[id]
    override suspend fun save(entity: E): E = entity.also { store[getId(it)] = it }
    override suspend fun delete(entity: E) { store.remove(getId(entity)) }
    override suspend fun deleteById(id: I) { store.remove(id) }
    override suspend fun existsById(id: I): Boolean = store.containsKey(id)
}

private data class TestItem(val id: String, val name: String)

class RepositoryContractTest : FunSpec({
    test("save and findById round-trip") {
        val repo = InMemoryRepository<TestItem, String> { it.id }
        val item = TestItem("1", "Item One")
        repo.save(item)
        repo.findById("1") shouldBe item
    }

    test("deleteById removes entity") {
        val repo = InMemoryRepository<TestItem, String> { it.id }
        repo.save(TestItem("1", "Item One"))
        repo.deleteById("1")
        repo.findById("1").shouldBeNull()
    }

    test("existsById returns false for missing entity") {
        val repo = InMemoryRepository<TestItem, String> { it.id }
        repo.existsById("missing") shouldBe false
    }

    test("PageResult computes totalPages correctly") {
        val page = PageResult(
            content = listOf(1, 2, 3),
            page = 0,
            size = 3,
            totalElements = 10,
        )
        page.totalPages shouldBe 4
        page.isFirst shouldBe true
        page.isLast shouldBe false
    }

    test("PageResult isLast on last page") {
        val page = PageResult(
            content = listOf(10),
            page = 3,
            size = 3,
            totalElements = 10,
        )
        page.isLast shouldBe true
        page.isFirst shouldBe false
        page.isEmpty shouldBe false
    }

    test("PageResult isEmpty for empty content") {
        val page = PageResult(
            content = emptyList<Int>(),
            page = 0,
            size = 5,
            totalElements = 0,
        )
        page.isEmpty shouldBe true
        page.totalPages shouldBe 0
    }

    test("PageRequest defaults are valid") {
        val req = PageRequest()
        req.page shouldBe 0
        req.size shouldBe 20
    }

    test("PageRequest rejects negative page") {
        shouldThrow<IllegalArgumentException> { PageRequest(page = -1) }
    }

    test("PageRequest rejects zero size") {
        shouldThrow<IllegalArgumentException> { PageRequest(size = 0) }
    }

    test("SortField defaults to ASC direction") {
        val sf = SortField("name")
        sf.direction shouldBe SortDirection.ASC
    }

    test("PageRequest with sort fields") {
        val req = PageRequest(sort = listOf(SortField("id", SortDirection.DESC)))
        req.sort.size shouldBe 1
        req.sort[0].direction shouldBe SortDirection.DESC
    }

    test("EntityNotFoundException is a PersistenceException") {
        val ex = EntityNotFoundException("not found")
        ex.message shouldBe "not found"
        ex.javaClass.superclass shouldBe PersistenceException::class.java
    }

    test("OptimisticLockException carries cause") {
        val cause = RuntimeException("conflict")
        val ex = OptimisticLockException("lock failed", cause)
        ex.cause shouldBe cause
    }

    test("delete removes entity") {
        val repo = InMemoryRepository<TestItem, String> { it.id }
        val item = TestItem("2", "Item Two")
        repo.save(item)
        repo.delete(item)
        repo.findById("2").shouldBeNull()
    }

    test("existsById returns true for existing entity") {
        val repo = InMemoryRepository<TestItem, String> { it.id }
        repo.save(TestItem("3", "Item Three"))
        repo.existsById("3") shouldBe true
    }
})
