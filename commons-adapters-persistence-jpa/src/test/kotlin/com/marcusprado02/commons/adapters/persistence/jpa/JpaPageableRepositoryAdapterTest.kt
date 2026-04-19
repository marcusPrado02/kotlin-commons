package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PageRequest
import com.marcusprado02.commons.ports.persistence.SortDirection
import com.marcusprado02.commons.ports.persistence.SortField
import com.marcusprado02.commons.testkit.testcontainers.PostgresContainers
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
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaPageableRepositoryAdapterTest {
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

    private lateinit var adapter: JpaPageableRepositoryAdapter<TestEntity, UUID>

    @BeforeEach
    fun setUp() {
        adapter = object : JpaPageableRepositoryAdapter<TestEntity, UUID>(jpaRepository) {}
        jpaRepository.deleteAll()
    }

    @Test
    fun `findAll returns page of results`() =
        runTest {
            repeat(5) { i -> jpaRepository.save(TestEntity(name = "item-$i")) }
            val result = adapter.findAll(PageRequest(page = 0, size = 3))
            assertEquals(3, result.content.size)
            assertEquals(5L, result.totalElements)
            assertEquals(2, result.totalPages)
            assertTrue(result.isFirst)
        }

    @Test
    fun `findAll second page returns remaining items`() =
        runTest {
            repeat(5) { i -> jpaRepository.save(TestEntity(name = "item-$i")) }
            val result = adapter.findAll(PageRequest(page = 1, size = 3))
            assertEquals(2, result.content.size)
            assertTrue(result.isLast)
        }

    @Test
    fun `count returns total number of entities`() =
        runTest {
            repeat(4) { i -> jpaRepository.save(TestEntity(name = "item-$i")) }
            assertEquals(4L, adapter.count())
        }

    @Test
    fun `findAll with ASC sort returns ordered results`() =
        runTest {
            jpaRepository.save(TestEntity(name = "beta"))
            jpaRepository.save(TestEntity(name = "alpha"))
            val result = adapter.findAll(PageRequest(page = 0, size = 10, sort = listOf(SortField("name", SortDirection.ASC))))
            assertEquals(2, result.content.size)
            assertEquals("alpha", result.content[0].name)
        }

    @Test
    fun `findAll with DESC sort returns reverse-ordered results`() =
        runTest {
            jpaRepository.save(TestEntity(name = "alpha"))
            jpaRepository.save(TestEntity(name = "beta"))
            val result = adapter.findAll(PageRequest(page = 0, size = 10, sort = listOf(SortField("name", SortDirection.DESC))))
            assertEquals(2, result.content.size)
            assertEquals("beta", result.content[0].name)
        }
}
