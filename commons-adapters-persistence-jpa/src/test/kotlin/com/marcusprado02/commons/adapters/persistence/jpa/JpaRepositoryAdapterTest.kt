package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.testkit.testcontainers.PostgresContainers
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaRepositoryAdapterTest {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { PostgresContainers.instance.jdbcUrl }
            registry.add("spring.datasource.username") { PostgresContainers.instance.username }
            registry.add("spring.datasource.password") { PostgresContainers.instance.password }
        }
    }

    @Autowired
    private lateinit var jpaRepository: TestJpaRepository

    private lateinit var adapter: JpaRepositoryAdapter<TestEntity, UUID>

    @BeforeEach
    fun setUp() {
        adapter = object : JpaRepositoryAdapter<TestEntity, UUID>(jpaRepository) {}
        jpaRepository.deleteAll()
    }

    @Test
    fun `save and findById round-trip`() =
        runTest {
            val entity = TestEntity(name = "hello")
            val saved = adapter.save(entity)
            val found = adapter.findById(saved.id!!)
            assertNotNull(found)
            assertEquals("hello", found.name)
        }

    @Test
    fun `findById returns null when not found`() =
        runTest {
            val result = adapter.findById(UUID.randomUUID())
            assertNull(result)
        }

    @Test
    fun `existsById returns true after save`() =
        runTest {
            val saved = adapter.save(TestEntity(name = "x"))
            assertTrue(adapter.existsById(saved.id!!))
        }

    @Test
    fun `existsById returns false when not found`() =
        runTest {
            val result = adapter.existsById(UUID.randomUUID())
            assertEquals(false, result)
        }

    @Test
    fun `deleteById removes entity`() =
        runTest {
            val saved = adapter.save(TestEntity(name = "del"))
            adapter.deleteById(saved.id!!)
            assertNull(adapter.findById(saved.id!!))
        }

    @Test
    fun `delete removes entity`() =
        runTest {
            val saved = adapter.save(TestEntity(name = "del2"))
            adapter.delete(saved)
            assertNull(adapter.findById(saved.id!!))
        }

    @Test
    fun `saveAll persists all entities and returns them`() =
        runTest {
            val entities = listOf(TestEntity(name = "batch1"), TestEntity(name = "batch2"), TestEntity(name = "batch3"))
            val saved = adapter.saveAll(entities)
            assertEquals(3, saved.size)
            saved.forEach { assertNotNull(it.id) }
            saved.map { it.name }.toSet() shouldBe setOf("batch1", "batch2", "batch3")
        }

    @Test
    fun `saveAll with empty list returns empty list`() =
        runTest {
            val result = adapter.saveAll(emptyList())
            assertTrue(result.isEmpty())
        }
}
