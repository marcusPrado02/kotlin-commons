# kotlin-commons Fase 2 — Adapters Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 6 new modules — a shared Testcontainers test-infra module plus 5 adapter modules implementing the Fase 1 ports — and a `kotlin-commons-spring.gradle.kts` convention plugin for Spring-based adapters.

**Architecture:** Each adapter wraps a technology-specific library behind a port interface defined in Fase 1. JPA and Redis adapters use Spring Data and apply `kotlin-commons-spring.gradle.kts`; Kafka, OkHttp, and SMTP adapters are framework-agnostic and apply `kotlin-commons.gradle.kts`. Blocking library calls are bridged to `suspend fun` via `withContext(Dispatchers.IO)` or `suspendCancellableCoroutine`. `commons-testkit-testcontainers` provides singleton container objects (one per JVM) reused across all adapter test suites.

**Tech Stack:** Kotlin 2.1.0, JVM 21, Spring Boot 3.5.0, Spring Data JPA, Spring Data Redis, kafka-clients 3.9.0, OkHttp 4.12.0, Jakarta Mail (Angus) 2.0.1, Testcontainers 1.20.4, GreenMail 2.1.2, Kotest 5.9.1, MockK 1.13.12, Kover 0.9.0.

---

## File Map

```
buildSrc/build.gradle.kts                                                      modify
buildSrc/src/main/kotlin/kotlin-commons-spring.gradle.kts                      create
gradle/libs.versions.toml                                                      modify
settings.gradle.kts                                                            modify
commons-bom/build.gradle.kts                                                   modify

commons-testkit-testcontainers/
  build.gradle.kts                                                             create
  src/main/kotlin/com/marcusprado02/commons/testkit/testcontainers/
    PostgresContainers.kt                                                      create
    RedisContainers.kt                                                         create
    KafkaContainers.kt                                                         create
    GreenMailContainers.kt                                                     create

commons-adapters-persistence-jpa/
  build.gradle.kts                                                             create
  src/main/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/
    JpaRepositoryAdapter.kt                                                    create
    JpaPageableRepositoryAdapter.kt                                            create
    Converters.kt                (internal — PageRequest/Page/Sort converters) create
  src/test/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/
    TestEntity.kt                                                              create
    TestJpaRepository.kt                                                       create
    JpaRepositoryAdapterTest.kt                                                create
    JpaPageableRepositoryAdapterTest.kt                                        create

commons-adapters-cache-redis/
  build.gradle.kts                                                             create
  src/main/kotlin/com/marcusprado02/commons/adapters/cache/redis/
    RedisCacheAdapter.kt                                                       create
  src/test/kotlin/com/marcusprado02/commons/adapters/cache/redis/
    RedisCacheAdapterTest.kt                                                   create

commons-adapters-messaging-kafka/
  build.gradle.kts                                                             create
  src/main/kotlin/com/marcusprado02/commons/adapters/messaging/kafka/
    KafkaMessagePublisherAdapter.kt                                            create
    KafkaMessageConsumerAdapter.kt                                             create
    TopicPartitionOffset.kt      (internal)                                   create
  src/test/kotlin/com/marcusprado02/commons/adapters/messaging/kafka/
    KafkaAdapterTest.kt                                                        create

commons-adapters-http-okhttp/
  build.gradle.kts                                                             create
  src/main/kotlin/com/marcusprado02/commons/adapters/http/okhttp/
    OkHttpClientAdapter.kt                                                     create
    RequestConverters.kt         (internal)                                   create
    ResponseConverters.kt        (internal)                                   create
  src/test/kotlin/com/marcusprado02/commons/adapters/http/okhttp/
    OkHttpClientAdapterTest.kt                                                 create

commons-adapters-email-smtp/
  build.gradle.kts                                                             create
  src/main/kotlin/com/marcusprado02/commons/adapters/email/smtp/
    SmtpEmailAdapter.kt                                                        create
    MimeMessageExtensions.kt     (internal)                                   create
  src/test/kotlin/com/marcusprado02/commons/adapters/email/smtp/
    SmtpEmailAdapterTest.kt                                                    create
```

---

### Task 1: Build Infrastructure

Sets up the version catalog entries, convention plugin, and wires all new modules into settings + BOM. No production logic — just configuration.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `buildSrc/build.gradle.kts`
- Create: `buildSrc/src/main/kotlin/kotlin-commons-spring.gradle.kts`
- Modify: `settings.gradle.kts`
- Modify: `commons-bom/build.gradle.kts`

- [ ] **Step 1: Add new versions and libraries to `gradle/libs.versions.toml`**

```toml
[versions]
kotlin = "2.1.0"
coroutines = "1.9.0"
kotest = "5.9.1"
mockk = "1.13.12"
kover = "0.9.0"
detekt = "1.23.7"
ktlint-gradle = "14.2.0"
spring-boot = "3.5.0"
spring-dependency-management = "1.1.7"
testcontainers = "1.20.4"
greenmail = "2.1.2"
okhttp = "4.12.0"
kafka-clients = "3.9.0"
angus-mail = "2.0.1"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotest-runner-junit5 = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }
testcontainers-core = { module = "org.testcontainers:testcontainers" }
testcontainers-postgresql = { module = "org.testcontainers:postgresql" }
testcontainers-kafka = { module = "org.testcontainers:kafka" }
greenmail-testcontainers = { module = "com.icegreen:greenmail-testcontainers", version.ref = "greenmail" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }
kafka-clients = { module = "org.apache.kafka:kafka-clients", version.ref = "kafka-clients" }
angus-mail = { module = "org.eclipse.angus:angus-mail", version.ref = "angus-mail" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint-gradle" }
```

- [ ] **Step 2: Update `buildSrc/build.gradle.kts` to add Spring plugin dependencies**

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// Plugin versions below MUST be kept in sync with gradle/libs.versions.toml
// buildSrc cannot consume the root project's version catalog (Gradle limitation)
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:14.2.0")
    // Spring convention plugin dependencies
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.1.0")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    // Allow convention plugins to access the root version catalog
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
```

- [ ] **Step 3: Create `buildSrc/src/main/kotlin/kotlin-commons-spring.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
    id("org.jetbrains.kotlin.plugin.spring")
    id("io.spring.dependency-management")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.findVersion("spring-boot").get()}")
    }
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
}
```

- [ ] **Step 4: Update `settings.gradle.kts` to include all 6 new modules**

```kotlin
rootProject.name = "kotlin-commons"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    "commons-kernel-core",
    "commons-kernel-errors",
    "commons-kernel-result",
    "commons-kernel-ddd",
    "commons-kernel-time",
    "commons-ports-persistence",
    "commons-ports-messaging",
    "commons-ports-http",
    "commons-ports-cache",
    "commons-ports-email",
    "commons-bom",
    "commons-testkit-testcontainers",
    "commons-adapters-persistence-jpa",
    "commons-adapters-cache-redis",
    "commons-adapters-messaging-kafka",
    "commons-adapters-http-okhttp",
    "commons-adapters-email-smtp",
)
```

- [ ] **Step 5: Update `commons-bom/build.gradle.kts` to add 6 new constraints**

```kotlin
plugins {
    `java-platform`
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    constraints {
        api(project(":commons-kernel-core"))
        api(project(":commons-kernel-errors"))
        api(project(":commons-kernel-result"))
        api(project(":commons-kernel-ddd"))
        api(project(":commons-kernel-time"))
        api(project(":commons-ports-persistence"))
        api(project(":commons-ports-messaging"))
        api(project(":commons-ports-http"))
        api(project(":commons-ports-cache"))
        api(project(":commons-ports-email"))
        api(project(":commons-testkit-testcontainers"))
        api(project(":commons-adapters-persistence-jpa"))
        api(project(":commons-adapters-cache-redis"))
        api(project(":commons-adapters-messaging-kafka"))
        api(project(":commons-adapters-http-okhttp"))
        api(project(":commons-adapters-email-smtp"))
    }
}
```

- [ ] **Step 6: Verify the build still compiles**

```bash
./gradlew :commons-bom:dependencies --configuration constraints
```

Expected: resolves without error. No tests run yet.

- [ ] **Step 7: Commit**

```bash
git add gradle/libs.versions.toml buildSrc/build.gradle.kts \
        buildSrc/src/main/kotlin/kotlin-commons-spring.gradle.kts \
        settings.gradle.kts commons-bom/build.gradle.kts
git commit -m "build: add Spring convention plugin and Fase 2 module scaffolding"
```

---

### Task 2: `commons-testkit-testcontainers`

Shared Testcontainers singleton objects. Code lives in `main` so adapter modules can declare `testImplementation(project(":commons-testkit-testcontainers"))`. This module has no Kover, ktlint, or detekt — plain Kotlin JVM only.

**Files:**
- Create: `commons-testkit-testcontainers/build.gradle.kts`
- Create: `commons-testkit-testcontainers/src/main/kotlin/com/marcusprado02/commons/testkit/testcontainers/PostgresContainers.kt`
- Create: `commons-testkit-testcontainers/src/main/kotlin/com/marcusprado02/commons/testkit/testcontainers/RedisContainers.kt`
- Create: `commons-testkit-testcontainers/src/main/kotlin/com/marcusprado02/commons/testkit/testcontainers/KafkaContainers.kt`
- Create: `commons-testkit-testcontainers/src/main/kotlin/com/marcusprado02/commons/testkit/testcontainers/GreenMailContainers.kt`

- [ ] **Step 1: Create `commons-testkit-testcontainers/build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.testcontainers.bom))
    implementation(libs.testcontainers.core)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.testcontainers.kafka)
    implementation(libs.greenmail.testcontainers)
}
```

- [ ] **Step 2: Create `PostgresContainers.kt`**

```kotlin
package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.PostgreSQLContainer

object PostgresContainers {
    val instance: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:16-alpine").also { it.start() }
    }
}
```

- [ ] **Step 3: Create `RedisContainers.kt`**

```kotlin
package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.GenericContainer

object RedisContainers {
    val instance: GenericContainer<*> by lazy {
        GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)
            .also { it.start() }
    }
}
```

- [ ] **Step 4: Create `KafkaContainers.kt`**

```kotlin
package com.marcusprado02.commons.testkit.testcontainers

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

object KafkaContainers {
    val instance: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .also { it.start() }
    }
}
```

- [ ] **Step 5: Create `GreenMailContainers.kt`**

```kotlin
package com.marcusprado02.commons.testkit.testcontainers

import com.icegreen.greenmail.testcontainers.GreenMailContainer

object GreenMailContainers {
    val instance: GreenMailContainer by lazy {
        GreenMailContainer().also { it.start() }
    }
}
```

- [ ] **Step 6: Verify testkit compiles**

```bash
./gradlew :commons-testkit-testcontainers:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add commons-testkit-testcontainers/
git commit -m "feat(testkit): add Testcontainers singleton objects for postgres, redis, kafka, smtp"
```

---

### Task 3: `commons-adapters-persistence-jpa`

Implements `Repository<E, I>` and `PageableRepository<E, I>` using Spring Data JPA. Blocking JPA calls dispatched to `Dispatchers.IO`. `DataAccessException` wrapped in `PersistenceException`.

**Files:**
- Create: `commons-adapters-persistence-jpa/build.gradle.kts`
- Create: `commons-adapters-persistence-jpa/src/main/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/JpaRepositoryAdapter.kt`
- Create: `commons-adapters-persistence-jpa/src/main/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/JpaPageableRepositoryAdapter.kt`
- Create: `commons-adapters-persistence-jpa/src/main/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/Converters.kt`
- Create: `commons-adapters-persistence-jpa/src/test/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/TestEntity.kt`
- Create: `commons-adapters-persistence-jpa/src/test/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/TestJpaRepository.kt`
- Create: `commons-adapters-persistence-jpa/src/test/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/JpaRepositoryAdapterTest.kt`
- Create: `commons-adapters-persistence-jpa/src/test/kotlin/com/marcusprado02/commons/adapters/persistence/jpa/JpaPageableRepositoryAdapterTest.kt`

- [ ] **Step 1: Create `build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons-spring")
}

dependencies {
    api(project(":commons-ports-persistence"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    runtimeOnly("org.postgresql:postgresql")
}
```

- [ ] **Step 2: Write the failing test — `JpaRepositoryAdapterTest.kt`**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.testkit.testcontainers.PostgresContainers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    fun `save and findById round-trip`() = runTest {
        val entity = TestEntity(name = "hello")
        val saved = adapter.save(entity)
        val found = adapter.findById(saved.id!!)
        assertNotNull(found)
        assertEquals("hello", found.name)
    }

    @Test
    fun `findById returns null when not found`() = runTest {
        val result = adapter.findById(UUID.randomUUID())
        assertNull(result)
    }

    @Test
    fun `existsById returns true after save`() = runTest {
        val saved = adapter.save(TestEntity(name = "x"))
        assertTrue(adapter.existsById(saved.id!!))
    }

    @Test
    fun `existsById returns false when not found`() = runTest {
        val result = adapter.existsById(UUID.randomUUID())
        assertEquals(false, result)
    }

    @Test
    fun `deleteById removes entity`() = runTest {
        val saved = adapter.save(TestEntity(name = "del"))
        adapter.deleteById(saved.id!!)
        assertNull(adapter.findById(saved.id!!))
    }

    @Test
    fun `delete removes entity`() = runTest {
        val saved = adapter.save(TestEntity(name = "del2"))
        adapter.delete(saved)
        assertNull(adapter.findById(saved.id!!))
    }
}
```

- [ ] **Step 3: Run test to verify it fails (class not found)**

```bash
./gradlew :commons-adapters-persistence-jpa:test 2>&1 | tail -20
```

Expected: FAIL — `JpaRepositoryAdapter` class does not exist yet.

- [ ] **Step 4: Create `TestEntity.kt` in test source set**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
data class TestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val name: String = "",
)
```

- [ ] **Step 5: Create `TestJpaRepository.kt` in test source set**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TestJpaRepository : JpaRepository<TestEntity, UUID>
```

- [ ] **Step 6: Create `Converters.kt`**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PageRequest
import com.marcusprado02.commons.ports.persistence.PageResult
import com.marcusprado02.commons.ports.persistence.SortDirection
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Page as SpringPage
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.data.domain.Pageable

internal fun PageRequest.toSpringPageable(): Pageable =
    if (sort.isEmpty()) {
        SpringPageRequest.of(page, size)
    } else {
        val springSort = Sort.by(
            sort.map { sf ->
                if (sf.direction == SortDirection.ASC) Sort.Order.asc(sf.field)
                else Sort.Order.desc(sf.field)
            },
        )
        SpringPageRequest.of(page, size, springSort)
    }

internal fun <E> SpringPage<E>.toPageResult(): PageResult<E> =
    PageResult(
        content = content,
        page = number,
        size = size,
        totalElements = totalElements,
    )
```

- [ ] **Step 7: Create `JpaRepositoryAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PersistenceException
import com.marcusprado02.commons.ports.persistence.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DataAccessException
import org.springframework.data.jpa.repository.JpaRepository

public abstract class JpaRepositoryAdapter<E : Any, I : Any>(
    private val jpa: JpaRepository<E, I>,
) : Repository<E, I> {

    override suspend fun findById(id: I): E? =
        withContext(Dispatchers.IO) {
            try {
                jpa.findById(id).orElse(null)
            } catch (ex: DataAccessException) {
                throw PersistenceException("findById failed", ex)
            }
        }

    override suspend fun save(entity: E): E =
        withContext(Dispatchers.IO) {
            try {
                jpa.save(entity)
            } catch (ex: DataAccessException) {
                throw PersistenceException("save failed", ex)
            }
        }

    override suspend fun delete(entity: E): Unit =
        withContext(Dispatchers.IO) {
            try {
                jpa.delete(entity)
            } catch (ex: DataAccessException) {
                throw PersistenceException("delete failed", ex)
            }
        }

    override suspend fun deleteById(id: I): Unit =
        withContext(Dispatchers.IO) {
            try {
                jpa.deleteById(id)
            } catch (ex: DataAccessException) {
                throw PersistenceException("deleteById failed", ex)
            }
        }

    override suspend fun existsById(id: I): Boolean =
        withContext(Dispatchers.IO) {
            try {
                jpa.existsById(id)
            } catch (ex: DataAccessException) {
                throw PersistenceException("existsById failed", ex)
            }
        }
}
```

- [ ] **Step 8: Run tests to verify `JpaRepositoryAdapterTest` passes**

```bash
./gradlew :commons-adapters-persistence-jpa:test --tests "*.JpaRepositoryAdapterTest" 2>&1 | tail -15
```

Expected: 6 tests pass.

- [ ] **Step 9: Write the failing pageable test — `JpaPageableRepositoryAdapterTest.kt`**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PageRequest
import com.marcusprado02.commons.testkit.testcontainers.PostgresContainers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    fun `findAll returns page of results`() = runTest {
        repeat(5) { i -> jpaRepository.save(TestEntity(name = "item-$i")) }
        val result = adapter.findAll(PageRequest(page = 0, size = 3))
        assertEquals(3, result.content.size)
        assertEquals(5L, result.totalElements)
        assertEquals(2, result.totalPages)
        assertTrue(result.isFirst)
    }

    @Test
    fun `findAll second page returns remaining items`() = runTest {
        repeat(5) { i -> jpaRepository.save(TestEntity(name = "item-$i")) }
        val result = adapter.findAll(PageRequest(page = 1, size = 3))
        assertEquals(2, result.content.size)
        assertTrue(result.isLast)
    }

    @Test
    fun `count returns total number of entities`() = runTest {
        repeat(4) { i -> jpaRepository.save(TestEntity(name = "item-$i")) }
        assertEquals(4L, adapter.count())
    }
}
```

- [ ] **Step 10: Create `JpaPageableRepositoryAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PageRequest
import com.marcusprado02.commons.ports.persistence.PageResult
import com.marcusprado02.commons.ports.persistence.PageableRepository
import com.marcusprado02.commons.ports.persistence.PersistenceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DataAccessException
import org.springframework.data.jpa.repository.JpaRepository

public abstract class JpaPageableRepositoryAdapter<E : Any, I : Any>(
    private val jpa: JpaRepository<E, I>,
) : JpaRepositoryAdapter<E, I>(jpa), PageableRepository<E, I> {

    override suspend fun findAll(request: PageRequest): PageResult<E> =
        withContext(Dispatchers.IO) {
            try {
                jpa.findAll(request.toSpringPageable()).toPageResult()
            } catch (ex: DataAccessException) {
                throw PersistenceException("findAll failed", ex)
            }
        }

    override suspend fun count(): Long =
        withContext(Dispatchers.IO) {
            try {
                jpa.count()
            } catch (ex: DataAccessException) {
                throw PersistenceException("count failed", ex)
            }
        }
}
```

- [ ] **Step 11: Run all tests in this module**

```bash
./gradlew :commons-adapters-persistence-jpa:test 2>&1 | tail -15
```

Expected: All tests pass.

- [ ] **Step 12: Verify koverVerify passes**

```bash
./gradlew :commons-adapters-persistence-jpa:koverVerify
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 13: Commit**

```bash
git add commons-adapters-persistence-jpa/
git commit -m "feat(adapters): add JPA adapter for Repository and PageableRepository ports"
```

---

### Task 4: `commons-adapters-cache-redis`

Implements `CachePort` using `RedisTemplate<String, ByteArray>` + `ObjectMapper`. All operations dispatched to `Dispatchers.IO`. `RedisConnectionFailureException` wrapped in `PersistenceException`.

**Files:**
- Create: `commons-adapters-cache-redis/build.gradle.kts`
- Create: `commons-adapters-cache-redis/src/main/kotlin/com/marcusprado02/commons/adapters/cache/redis/RedisCacheAdapter.kt`
- Create: `commons-adapters-cache-redis/src/test/kotlin/com/marcusprado02/commons/adapters/cache/redis/RedisCacheAdapterTest.kt`

- [ ] **Step 1: Create `build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons-spring")
}

dependencies {
    api(project(":commons-ports-cache"))
    api(project(":commons-ports-persistence"))
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 2: Write the failing test — `RedisCacheAdapterTest.kt`**

```kotlin
package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.marcusprado02.commons.ports.cache.CacheKey
import com.marcusprado02.commons.testkit.testcontainers.RedisContainers
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

class RedisCacheAdapterTest : FunSpec({
    val container = RedisContainers.instance
    val connectionFactory = LettuceConnectionFactory(
        RedisStandaloneConfiguration(container.host, container.getMappedPort(6379)),
    ).also { it.afterPropertiesSet() }
    val redis = RedisTemplate<String, ByteArray>().also {
        it.connectionFactory = connectionFactory
        it.keySerializer = StringRedisSerializer()
        it.afterPropertiesSet()
    }
    val objectMapper: ObjectMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .build()
    val adapter = RedisCacheAdapter(redis, objectMapper)

    beforeTest {
        redis.execute { it.serverCommands().flushDb() }
    }

    afterSpec {
        connectionFactory.destroy()
    }

    test("put and get round-trip with String") {
        runTest {
            adapter.put(CacheKey("k1"), "hello")
            val result = adapter.get(CacheKey("k1"), String::class.java)
            result shouldBe "hello"
        }
    }

    test("get returns null for missing key") {
        runTest {
            val result = adapter.get(CacheKey("missing"), String::class.java)
            result shouldBe null
        }
    }

    test("remove deletes the key") {
        runTest {
            adapter.put(CacheKey("k2"), "value")
            adapter.remove(CacheKey("k2"))
            adapter.get(CacheKey("k2"), String::class.java) shouldBe null
        }
    }

    test("exists returns true after put") {
        runTest {
            adapter.put(CacheKey("k3"), 42)
            adapter.exists(CacheKey("k3")) shouldBe true
        }
    }

    test("exists returns false for missing key") {
        runTest {
            adapter.exists(CacheKey("gone")) shouldBe false
        }
    }

    test("clear removes all keys") {
        runTest {
            adapter.put(CacheKey("a"), "x")
            adapter.put(CacheKey("b"), "y")
            adapter.clear()
            adapter.get(CacheKey("a"), String::class.java) shouldBe null
        }
    }

    test("put with TTL stores value that can be retrieved") {
        runTest {
            adapter.put(CacheKey("ttl-key"), "temporary", ttl = Duration.ofSeconds(30))
            adapter.get(CacheKey("ttl-key"), String::class.java) shouldNotBe null
        }
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-adapters-cache-redis:test 2>&1 | tail -10
```

Expected: FAIL — `RedisCacheAdapter` class does not exist.

- [ ] **Step 4: Create `RedisCacheAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.marcusprado02.commons.ports.cache.CacheKey
import com.marcusprado02.commons.ports.cache.CachePort
import com.marcusprado02.commons.ports.persistence.PersistenceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

public class RedisCacheAdapter(
    private val redis: RedisTemplate<String, ByteArray>,
    private val objectMapper: ObjectMapper,
) : CachePort {

    override suspend fun <T : Any> get(key: CacheKey, type: Class<T>): T? =
        withContext(Dispatchers.IO) {
            try {
                redis.opsForValue().get(key.value)?.let { objectMapper.readValue(it, type) }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis get failed for key '${key.value}'", ex)
            }
        }

    override suspend fun <T : Any> put(key: CacheKey, value: T, ttl: Duration?): Unit =
        withContext(Dispatchers.IO) {
            try {
                val bytes = objectMapper.writeValueAsBytes(value)
                if (ttl != null) {
                    redis.opsForValue().set(key.value, bytes, ttl)
                } else {
                    redis.opsForValue().set(key.value, bytes)
                }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis put failed for key '${key.value}'", ex)
            }
        }

    override suspend fun remove(key: CacheKey): Unit =
        withContext(Dispatchers.IO) {
            try {
                redis.delete(key.value)
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis remove failed for key '${key.value}'", ex)
            }
        }

    override suspend fun clear(): Unit =
        withContext(Dispatchers.IO) {
            try {
                redis.execute { it.serverCommands().flushDb() }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis clear failed", ex)
            }
        }

    override suspend fun exists(key: CacheKey): Boolean =
        withContext(Dispatchers.IO) {
            try {
                redis.hasKey(key.value) == true
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis exists failed for key '${key.value}'", ex)
            }
        }
}
```

- [ ] **Step 5: Run tests and verify they pass**

```bash
./gradlew :commons-adapters-cache-redis:test 2>&1 | tail -15
```

Expected: 7 tests pass.

- [ ] **Step 6: Verify koverVerify**

```bash
./gradlew :commons-adapters-cache-redis:koverVerify
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add commons-adapters-cache-redis/
git commit -m "feat(adapters): add Redis adapter for CachePort"
```

---

### Task 5: `commons-adapters-messaging-kafka`

Implements `MessagePublisherPort` and `MessageConsumerPort` using `kafka-clients`. No Spring. Publisher uses `suspendCancellableCoroutine` with Kafka's async callback. Consumer uses `withContext(Dispatchers.IO)` + manual offset commit.

**Files:**
- Create: `commons-adapters-messaging-kafka/build.gradle.kts`
- Create: `commons-adapters-messaging-kafka/src/main/kotlin/com/marcusprado02/commons/adapters/messaging/kafka/TopicPartitionOffset.kt`
- Create: `commons-adapters-messaging-kafka/src/main/kotlin/com/marcusprado02/commons/adapters/messaging/kafka/KafkaMessagePublisherAdapter.kt`
- Create: `commons-adapters-messaging-kafka/src/main/kotlin/com/marcusprado02/commons/adapters/messaging/kafka/KafkaMessageConsumerAdapter.kt`
- Create: `commons-adapters-messaging-kafka/src/test/kotlin/com/marcusprado02/commons/adapters/messaging/kafka/KafkaAdapterTest.kt`

- [ ] **Step 1: Create `build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-ports-messaging"))
    api(libs.kafka.clients)
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 2: Write the failing test — `KafkaAdapterTest.kt`**

```kotlin
package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.ConsumerGroup
import com.marcusprado02.commons.ports.messaging.MessageHeaders
import com.marcusprado02.commons.ports.messaging.MessageId
import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.TopicName
import com.marcusprado02.commons.testkit.testcontainers.KafkaContainers
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Instant

class KafkaAdapterTest : FunSpec({
    val bootstrap = KafkaContainers.instance.bootstrapServers
    val topic = TopicName("test-topic")
    val group = ConsumerGroup("test-group")

    val producer = KafkaProducer<String, ByteArray>(
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java.name,
        ),
    )
    val consumer = KafkaConsumer<String, ByteArray>(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
            ConsumerConfig.GROUP_ID_CONFIG to group.value,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ),
    )

    val publisher = KafkaMessagePublisherAdapter(producer)
    val consumerAdapter = KafkaMessageConsumerAdapter(consumer, group.value)

    afterSpec {
        producer.close()
        consumer.close()
    }

    test("publish sends a message that can be received") {
        runTest {
            val envelope = MessageEnvelope(
                topic = topic,
                body = "hello-kafka".toByteArray(),
                headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
            )
            publisher.publish(envelope)
            val received = consumerAdapter.receive(topic, group)
            received shouldNotBe null
            String(received!!.body) shouldBe "hello-kafka"
        }
    }

    test("acknowledge commits offset without error") {
        runTest {
            val envelope = MessageEnvelope(
                topic = topic,
                body = "ack-test".toByteArray(),
                headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
            )
            publisher.publish(envelope)
            val received = consumerAdapter.receive(topic, group)
            received shouldNotBe null
            consumerAdapter.acknowledge(received!!.headers.messageId)
        }
    }

    test("publishBatch sends all messages") {
        runTest {
            val envelopes = (1..3).map { i ->
                MessageEnvelope(
                    topic = topic,
                    body = "batch-$i".toByteArray(),
                    headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now()),
                )
            }
            publisher.publishBatch(envelopes)
            var count = 0
            repeat(3) {
                val msg = consumerAdapter.receive(topic, group)
                if (msg != null) count++
            }
            count shouldBe 3
        }
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-adapters-messaging-kafka:test 2>&1 | tail -10
```

Expected: FAIL — adapter classes do not exist.

- [ ] **Step 4: Create `TopicPartitionOffset.kt`**

```kotlin
package com.marcusprado02.commons.adapters.messaging.kafka

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

internal data class TopicPartitionOffset(
    val topic: String,
    val partition: Int,
    val offset: Long,
) {
    fun toOffsetMap(): Map<TopicPartition, OffsetAndMetadata> =
        mapOf(TopicPartition(topic, partition) to OffsetAndMetadata(offset + 1))
}
```

- [ ] **Step 5: Create `KafkaMessagePublisherAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.MessagePublisherPort
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public class KafkaMessagePublisherAdapter(
    private val producer: KafkaProducer<String, ByteArray>,
) : MessagePublisherPort {

    override suspend fun publish(envelope: MessageEnvelope<*>): Unit =
        suspendCancellableCoroutine { cont ->
            val body = envelope.body as? ByteArray
                ?: throw IllegalArgumentException(
                    "KafkaMessagePublisherAdapter requires ByteArray body, got ${envelope.body?.javaClass?.name}",
                )
            val record = ProducerRecord(
                envelope.topic.value,
                envelope.headers.messageId.value,
                body,
            )
            producer.send(record) { _, ex ->
                if (ex != null) cont.resumeWithException(ex) else cont.resume(Unit)
            }
        }

    override suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>): Unit =
        coroutineScope { envelopes.forEach { launch { publish(it) } } }
}
```

- [ ] **Step 6: Create `KafkaMessageConsumerAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.messaging.kafka

import com.marcusprado02.commons.ports.messaging.ConsumerGroup
import com.marcusprado02.commons.ports.messaging.MessageEnvelope
import com.marcusprado02.commons.ports.messaging.MessageHeaders
import com.marcusprado02.commons.ports.messaging.MessageId
import com.marcusprado02.commons.ports.messaging.MessageConsumerPort
import com.marcusprado02.commons.ports.messaging.TopicName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

public class KafkaMessageConsumerAdapter(
    private val consumer: KafkaConsumer<String, ByteArray>,
    private val groupId: String,
) : MessageConsumerPort {

    private val pending = ConcurrentHashMap<String, TopicPartitionOffset>()

    override suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>? =
        withContext(Dispatchers.IO) {
            require(group.value == groupId) {
                "Consumer group mismatch: adapter configured for '$groupId', received '${group.value}'"
            }
            consumer.subscribe(listOf(topic.value))
            consumer.poll(Duration.ofMillis(500))
                .firstOrNull()
                ?.let { record ->
                    val id = MessageId(record.key() ?: UUID.randomUUID().toString())
                    pending[id.value] = TopicPartitionOffset(record.topic(), record.partition(), record.offset())
                    MessageEnvelope(
                        topic = topic,
                        body = record.value(),
                        headers = MessageHeaders(
                            messageId = id,
                            timestamp = Instant.ofEpochMilli(record.timestamp()),
                        ),
                    )
                }
        }

    override suspend fun acknowledge(messageId: MessageId): Unit =
        withContext(Dispatchers.IO) {
            pending.remove(messageId.value)?.let { tpo ->
                consumer.commitSync(tpo.toOffsetMap())
            }
        }

    override suspend fun nack(messageId: MessageId) {
        pending.remove(messageId.value)
        // offset is not committed — Kafka redelivers on next consumer session (at-least-once)
    }
}
```

- [ ] **Step 7: Run tests and verify they pass**

```bash
./gradlew :commons-adapters-messaging-kafka:test 2>&1 | tail -15
```

Expected: 3 tests pass.

- [ ] **Step 8: Verify koverVerify**

```bash
./gradlew :commons-adapters-messaging-kafka:koverVerify
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add commons-adapters-messaging-kafka/
git commit -m "feat(adapters): add Kafka adapter for MessagePublisherPort and MessageConsumerPort"
```

---

### Task 6: `commons-adapters-http-okhttp`

Implements `HttpClientPort` using OkHttp. `execute` uses `suspendCancellableCoroutine` with OkHttp's async enqueue. Coroutine cancellation is propagated via `call.cancel()`. All `HttpBody` variants are converted to OkHttp `RequestBody`. Per-request timeout is applied via `OkHttpClient.newBuilder()`.

**Files:**
- Create: `commons-adapters-http-okhttp/build.gradle.kts`
- Create: `commons-adapters-http-okhttp/src/main/kotlin/com/marcusprado02/commons/adapters/http/okhttp/OkHttpClientAdapter.kt`
- Create: `commons-adapters-http-okhttp/src/main/kotlin/com/marcusprado02/commons/adapters/http/okhttp/RequestConverters.kt`
- Create: `commons-adapters-http-okhttp/src/main/kotlin/com/marcusprado02/commons/adapters/http/okhttp/ResponseConverters.kt`
- Create: `commons-adapters-http-okhttp/src/test/kotlin/com/marcusprado02/commons/adapters/http/okhttp/OkHttpClientAdapterTest.kt`

- [ ] **Step 1: Create `build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-ports-http"))
    api(libs.okhttp)
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 2: Write the failing test — `OkHttpClientAdapterTest.kt`**

```kotlin
package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpBody
import com.marcusprado02.commons.ports.http.HttpMethod
import com.marcusprado02.commons.ports.http.HttpRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.net.URI

class OkHttpClientAdapterTest : FunSpec({
    val server = MockWebServer()
    val client = OkHttpClient()
    val adapter = OkHttpClientAdapter(client)

    beforeSpec { server.start() }
    afterSpec { server.shutdown() }

    fun url(path: String = "/") = URI.create(server.url(path).toString())

    test("GET request returns 200 with body") {
        runTest {
            server.enqueue(MockResponse().setBody("hello").setResponseCode(200))
            val response = adapter.execute(HttpRequest(url(), HttpMethod.GET))
            response.statusCode shouldBe 200
            String(response.body!!) shouldBe "hello"
        }
    }

    test("POST request with JSON body") {
        runTest {
            server.enqueue(MockResponse().setResponseCode(201))
            val request = HttpRequest(
                uri = url("/items"),
                method = HttpMethod.POST,
                body = HttpBody.Bytes("""{"name":"test"}""".toByteArray(), "application/json"),
            )
            val response = adapter.execute(request)
            response.statusCode shouldBe 201
            val recorded = server.takeRequest()
            recorded.method shouldBe "POST"
            String(recorded.body.readByteArray()) shouldBe """{"name":"test"}"""
        }
    }

    test("execute with mapper transforms body") {
        runTest {
            server.enqueue(MockResponse().setBody("42").setResponseCode(200))
            val response = adapter.execute(HttpRequest(url(), HttpMethod.GET)) { bytes ->
                String(bytes).trim().toInt()
            }
            response.statusCode shouldBe 200
            response.body shouldBe 42
        }
    }

    test("404 response is client error") {
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            val response = adapter.execute(HttpRequest(url("/missing"), HttpMethod.GET))
            response.isClientError shouldBe true
            response.isSuccessful shouldBe false
        }
    }

    test("POST with FormUrlEncoded body") {
        runTest {
            server.enqueue(MockResponse().setResponseCode(200))
            val request = HttpRequest(
                uri = url("/form"),
                method = HttpMethod.POST,
                body = HttpBody.FormUrlEncoded(mapOf("key" to "value")),
            )
            adapter.execute(request)
            val recorded = server.takeRequest()
            recorded.body.readUtf8() shouldBe "key=value"
        }
    }

    test("response headers are mapped") {
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).addHeader("X-Custom", "yes"))
            val response = adapter.execute(HttpRequest(url(), HttpMethod.GET))
            response.headers["x-custom"] shouldNotBe null
        }
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-adapters-http-okhttp:test 2>&1 | tail -10
```

Expected: FAIL — `OkHttpClientAdapter` does not exist.

- [ ] **Step 4: Create `RequestConverters.kt`**

```kotlin
package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpBody
import com.marcusprado02.commons.ports.http.HttpMethod
import com.marcusprado02.commons.ports.http.HttpRequest
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal fun HttpRequest.toOkHttpRequest(): Request {
    val builder = Request.Builder().url(uri.toString())
    headers.forEach { (name, value) -> builder.addHeader(name, value) }
    builder.method(method.name, body?.toRequestBody(method))
    return builder.build()
}

private fun HttpBody.toRequestBody(method: HttpMethod): RequestBody? =
    when (this) {
        is HttpBody.Bytes -> content.toRequestBody(contentType.toMediaType())
        is HttpBody.FormUrlEncoded -> FormBody.Builder()
            .also { fb -> params.forEach { (k, v) -> fb.add(k, v) } }
            .build()
        is HttpBody.Multipart -> MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .also { mb ->
                parts.forEach { part ->
                    if (part.filename != null) {
                        mb.addFormDataPart(
                            part.name,
                            part.filename,
                            part.content.toRequestBody(part.contentType.toMediaType()),
                        )
                    } else {
                        mb.addFormDataPart(part.name, String(part.content))
                    }
                }
            }
            .build()
    }
```

- [ ] **Step 5: Create `ResponseConverters.kt`**

```kotlin
package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpResponse
import okhttp3.Response

internal fun Response.toHttpResponse(): HttpResponse<ByteArray> {
    val body = body?.bytes()
    close()
    val headers = headers.toMultimap()
    return HttpResponse(
        statusCode = code,
        headers = headers,
        body = body,
    )
}

internal fun <T> HttpResponse<ByteArray>.map(mapper: (ByteArray) -> T): HttpResponse<T> =
    HttpResponse(
        statusCode = statusCode,
        headers = headers,
        body = body?.let(mapper),
    )
```

- [ ] **Step 6: Create `OkHttpClientAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpClientPort
import com.marcusprado02.commons.ports.http.HttpRequest
import com.marcusprado02.commons.ports.http.HttpResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public class OkHttpClientAdapter(
    private val client: OkHttpClient,
) : HttpClientPort {

    override suspend fun execute(request: HttpRequest): HttpResponse<ByteArray> =
        suspendCancellableCoroutine { cont ->
            val effectiveClient = if (request.timeout != null) {
                client.newBuilder()
                    .callTimeout(request.timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .build()
            } else {
                client
            }
            val call = effectiveClient.newCall(request.toOkHttpRequest())
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) = cont.resumeWithException(e)
                override fun onResponse(call: Call, response: Response) = cont.resume(response.toHttpResponse())
            })
        }

    override suspend fun <T> execute(request: HttpRequest, mapper: (ByteArray) -> T): HttpResponse<T> =
        execute(request).map(mapper)
}
```

- [ ] **Step 7: Run tests and verify they pass**

```bash
./gradlew :commons-adapters-http-okhttp:test 2>&1 | tail -15
```

Expected: 6 tests pass.

- [ ] **Step 8: Verify koverVerify**

```bash
./gradlew :commons-adapters-http-okhttp:koverVerify
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add commons-adapters-http-okhttp/
git commit -m "feat(adapters): add OkHttp adapter for HttpClientPort"
```

---

### Task 7: `commons-adapters-email-smtp`

Implements `EmailPort` using Jakarta Mail (`org.eclipse.angus:angus-mail`). `send` and `sendBatch` dispatched to `Dispatchers.IO`. Handles plain text, HTML, BCC, ReplyTo, and attachments.

**Files:**
- Create: `commons-adapters-email-smtp/build.gradle.kts`
- Create: `commons-adapters-email-smtp/src/main/kotlin/com/marcusprado02/commons/adapters/email/smtp/SmtpEmailAdapter.kt`
- Create: `commons-adapters-email-smtp/src/main/kotlin/com/marcusprado02/commons/adapters/email/smtp/MimeMessageExtensions.kt`
- Create: `commons-adapters-email-smtp/src/test/kotlin/com/marcusprado02/commons/adapters/email/smtp/SmtpEmailAdapterTest.kt`

- [ ] **Step 1: Create `build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-ports-email"))
    api(libs.angus.mail)
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 2: Write the failing test — `SmtpEmailAdapterTest.kt`**

```kotlin
package com.marcusprado02.commons.adapters.email.smtp

import com.icegreen.greenmail.testcontainers.GreenMailContainer
import com.marcusprado02.commons.ports.email.Email
import com.marcusprado02.commons.ports.email.EmailAddress
import com.marcusprado02.commons.ports.email.EmailAttachment
import com.marcusprado02.commons.ports.email.EmailContent
import com.marcusprado02.commons.testkit.testcontainers.GreenMailContainers
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import jakarta.mail.Session
import kotlinx.coroutines.test.runTest
import java.util.Properties

class SmtpEmailAdapterTest : FunSpec({
    val greenMail: GreenMailContainer = GreenMailContainers.instance

    fun session(): Session = Session.getInstance(
        Properties().apply {
            put("mail.smtp.host", greenMail.host)
            put("mail.smtp.port", greenMail.getMappedPort(3025))
            put("mail.smtp.auth", "false")
        },
    )

    val adapter = SmtpEmailAdapter(session())

    beforeTest {
        greenMail.greenMail.purgeEmailFromAllMailboxes()
    }

    test("send plain text email") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "Hello",
                    content = EmailContent(plain = "Plain body"),
                ),
            )
            val messages = greenMail.greenMail.receivedMessages
            messages.size shouldBe 1
            messages[0].subject shouldBe "Hello"
        }
    }

    test("send HTML email") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "HTML",
                    content = EmailContent(html = "<b>Bold</b>"),
                ),
            )
            greenMail.greenMail.receivedMessages.size shouldBe 1
        }
    }

    test("send email with attachment") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to@example.com")),
                    subject = "With attachment",
                    content = EmailContent(plain = "See attached"),
                    attachments = listOf(
                        EmailAttachment(
                            filename = "file.txt",
                            content = "data".toByteArray(),
                            contentType = "text/plain",
                        ),
                    ),
                ),
            )
            greenMail.greenMail.receivedMessages.size shouldBe 1
        }
    }

    test("sendBatch sends all emails") {
        runTest {
            val emails = (1..3).map { i ->
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("to$i@example.com")),
                    subject = "Batch $i",
                    content = EmailContent(plain = "Body $i"),
                )
            }
            adapter.sendBatch(emails)
            greenMail.greenMail.receivedMessages.size shouldBe 3
        }
    }

    test("send to multiple recipients and cc") {
        runTest {
            adapter.send(
                Email(
                    from = EmailAddress("from@example.com"),
                    to = listOf(EmailAddress("a@example.com"), EmailAddress("b@example.com")),
                    subject = "Multi",
                    content = EmailContent(plain = "hi"),
                    cc = listOf(EmailAddress("c@example.com")),
                ),
            )
            greenMail.greenMail.receivedMessages.size shouldBe 3
        }
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-adapters-email-smtp:test 2>&1 | tail -10
```

Expected: FAIL — `SmtpEmailAdapter` does not exist.

- [ ] **Step 4: Create `MimeMessageExtensions.kt`**

```kotlin
package com.marcusprado02.commons.adapters.email.smtp

import com.marcusprado02.commons.ports.email.EmailAttachment
import com.marcusprado02.commons.ports.email.EmailContent
import jakarta.activation.DataHandler
import jakarta.mail.Part
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.util.ByteArrayDataSource

internal fun Part.applyContent(content: EmailContent) {
    when {
        content.html != null -> setContent(content.html, "text/html; charset=utf-8")
        else -> setText(content.plain, "utf-8", "plain")
    }
}

internal fun EmailAttachment.toMimeBodyPart(): MimeBodyPart =
    MimeBodyPart().also { part ->
        part.dataHandler = DataHandler(ByteArrayDataSource(content, contentType))
        part.fileName = filename
    }
```

- [ ] **Step 5: Create `SmtpEmailAdapter.kt`**

```kotlin
package com.marcusprado02.commons.adapters.email.smtp

import com.marcusprado02.commons.ports.email.Email
import com.marcusprado02.commons.ports.email.EmailPort
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class SmtpEmailAdapter(
    private val session: Session,
) : EmailPort {

    override suspend fun send(email: Email): Unit =
        withContext(Dispatchers.IO) {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(email.from.value))
            email.to.forEach { message.addRecipient(Message.RecipientType.TO, InternetAddress(it.value)) }
            email.cc.forEach { message.addRecipient(Message.RecipientType.CC, InternetAddress(it.value)) }
            email.bcc.forEach { message.addRecipient(Message.RecipientType.BCC, InternetAddress(it.value)) }
            email.replyTo?.let { message.replyTo = arrayOf(InternetAddress(it.value)) }
            message.subject = email.subject

            if (email.attachments.isEmpty()) {
                message.applyContent(email.content)
            } else {
                val multipart = MimeMultipart()
                multipart.addBodyPart(MimeBodyPart().also { it.applyContent(email.content) })
                email.attachments.forEach { multipart.addBodyPart(it.toMimeBodyPart()) }
                message.setContent(multipart)
            }

            Transport.send(message)
        }

    override suspend fun sendBatch(emails: List<Email>): Unit =
        withContext(Dispatchers.IO) {
            emails.forEach { send(it) }
        }
}
```

- [ ] **Step 6: Run tests and verify they pass**

```bash
./gradlew :commons-adapters-email-smtp:test 2>&1 | tail -15
```

Expected: 5 tests pass.

- [ ] **Step 7: Verify koverVerify**

```bash
./gradlew :commons-adapters-email-smtp:koverVerify
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add commons-adapters-email-smtp/
git commit -m "feat(adapters): add SMTP adapter for EmailPort using Jakarta Mail"
```

---

### Task 8: Full Build Verification

Runs the complete Gradle build across all 17 modules to verify no regressions in Fase 1 modules and that all Fase 2 modules pass ktlint, detekt, tests, and koverVerify.

**Files:** None new.

- [ ] **Step 1: Run the full build**

```bash
./gradlew build 2>&1 | grep -E "> Task.*FAILED|BUILD (SUCCESSFUL|FAILED)"
```

Expected: `BUILD SUCCESSFUL`. If any task fails, fix it before proceeding.

- [ ] **Step 2: Verify all modules have passing tests individually**

```bash
./gradlew test 2>&1 | grep -E "tests were|BUILD"
```

Expected: All modules report test results. `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit if there were any lint/style fixes from step 1**

```bash
git add -A
git commit -m "fix(build): resolve any ktlint/detekt violations in Fase 2 modules"
```

Only run this step if `git status` shows changes. If the build was clean, skip this commit.
