# kotlin-commons — Implementation Plan (Fase 1)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar a infraestrutura de build e 10 módulos (5 kernel + 5 ports) do kotlin-commons Fase 1, com APIs Kotlin idiomáticas e cobertura de testes mínima de 60% linha / 55% branch.

**Architecture:** Multi-módulo Gradle com Kotlin DSL. Convention plugins em `buildSrc` uniformizam configuração. Camada kernel tem zero dependências externas; ports dependem de `kotlinx-coroutines-core` para `suspend fun`. `Result<T>`, `Either<L,R>`, `Option<T>` implementados do zero — sem Arrow.

**Tech Stack:** Kotlin 2.1.0, JVM 21, Gradle 8.11, kotlinx-coroutines 1.9.0, Kotest 5.9.1, MockK 1.13.12, Kover 0.9.0, ktlint, detekt.

---

## File Map

```
kotlin-commons/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── buildSrc/
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── kotlin-commons.gradle.kts
│
├── commons-kernel-core/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/marcusprado02/commons/kernel/core/
│       │   ├── strings.kt
│       │   ├── uuids.kt
│       │   ├── numbers.kt
│       │   ├── collections.kt
│       │   └── preconditions.kt
│       └── test/kotlin/com/marcusprado02/commons/kernel/core/
│           ├── StringsTest.kt
│           ├── UuidsTest.kt
│           └── PreconditionsTest.kt
│
├── commons-kernel-errors/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/marcusprado02/commons/kernel/errors/
│       │   ├── ErrorCode.kt
│       │   ├── ErrorCategory.kt
│       │   ├── Severity.kt
│       │   ├── ProblemDetail.kt
│       │   ├── Problem.kt
│       │   ├── Problems.kt
│       │   ├── StandardErrorCodes.kt
│       │   └── exceptions.kt
│       └── test/kotlin/com/marcusprado02/commons/kernel/errors/
│           ├── ProblemTest.kt
│           └── ExceptionsTest.kt
│
├── commons-kernel-result/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/marcusprado02/commons/kernel/result/
│       │   ├── Result.kt
│       │   ├── Either.kt
│       │   └── Option.kt
│       └── test/kotlin/com/marcusprado02/commons/kernel/result/
│           ├── ResultTest.kt
│           ├── EitherTest.kt
│           └── OptionTest.kt
│
├── commons-kernel-ddd/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/marcusprado02/commons/kernel/ddd/
│       │   ├── identity/TenantId.kt
│       │   ├── identity/ActorId.kt
│       │   ├── identity/CorrelationId.kt
│       │   ├── identity/UuidIdentifier.kt
│       │   ├── audit/AuditStamp.kt
│       │   ├── audit/DeletionStamp.kt
│       │   ├── audit/AuditTrail.kt
│       │   ├── entity/EntityVersion.kt
│       │   ├── entity/Entity.kt
│       │   ├── aggregate/AggregateSnapshot.kt
│       │   ├── aggregate/AggregateRoot.kt
│       │   ├── valueobject/ValueObject.kt
│       │   ├── event/EventId.kt
│       │   ├── event/EventMetadata.kt
│       │   ├── event/DomainEvent.kt
│       │   ├── specification/Specification.kt
│       │   ├── invariant/Invariant.kt
│       │   ├── context/ActorProvider.kt
│       │   ├── context/TenantProvider.kt
│       │   └── context/CorrelationProvider.kt
│       └── test/kotlin/com/marcusprado02/commons/kernel/ddd/
│           ├── EntityTest.kt
│           ├── AggregateRootTest.kt
│           ├── SpecificationTest.kt
│           └── InvariantTest.kt
│
├── commons-kernel-time/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/marcusprado02/commons/kernel/time/
│       │   ├── ClockProvider.kt
│       │   ├── SystemClockProvider.kt
│       │   ├── FixedClockProvider.kt
│       │   └── TimeWindow.kt
│       └── test/kotlin/com/marcusprado02/commons/kernel/time/
│           └── TimeWindowTest.kt
│
├── commons-ports-persistence/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/marcusprado02/commons/ports/persistence/
│       ├── Repository.kt
│       ├── PageableRepository.kt
│       ├── PageRequest.kt
│       ├── PageResult.kt
│       ├── QuerySpecification.kt
│       └── PersistenceException.kt
│
├── commons-ports-messaging/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/marcusprado02/commons/ports/messaging/
│       ├── MessageId.kt
│       ├── TopicName.kt
│       ├── MessageHeaders.kt
│       ├── MessageEnvelope.kt
│       ├── MessagePublisherPort.kt
│       └── MessageConsumerPort.kt
│
├── commons-ports-http/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/marcusprado02/commons/ports/http/
│       ├── HttpMethod.kt
│       ├── HttpBody.kt
│       ├── HttpRequest.kt
│       ├── HttpResponse.kt
│       └── HttpClientPort.kt
│
├── commons-ports-cache/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/marcusprado02/commons/ports/cache/
│       ├── CacheKey.kt
│       └── CachePort.kt
│
├── commons-ports-email/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/marcusprado02/commons/ports/email/
│       ├── EmailAddress.kt
│       ├── EmailContent.kt
│       ├── EmailAttachment.kt
│       ├── Email.kt
│       └── EmailPort.kt
│
└── commons-bom/
    └── build.gradle.kts
```

---

### Task 1: Build Infrastructure

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `buildSrc/build.gradle.kts`
- Create: `buildSrc/src/main/kotlin/kotlin-commons.gradle.kts`
- Create: empty `build.gradle.kts` + source dirs for all 11 modules

- [ ] **Step 1: Generate Gradle wrapper**

```bash
gradle wrapper --gradle-version 8.11 --distribution-type bin
```
Se Gradle não estiver disponível localmente: `sdk install gradle 8.11` (SDKMAN) ou `brew install gradle`.

- [ ] **Step 2: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "kotlin-commons"

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
)
```

- [ ] **Step 3: Create `gradle/libs.versions.toml`**

```toml
[versions]
kotlin = "2.1.0"
coroutines = "1.9.0"
kotest = "5.9.1"
mockk = "1.13.12"
kover = "0.9.0"
detekt = "1.23.7"
ktlint-gradle = "12.1.1"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotest-runner-junit5 = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint-gradle" }
```

- [ ] **Step 4: Create `buildSrc/build.gradle.kts`**

```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.jlleitschuh.gradle.ktlint:ktlint-gradle:12.1.1")
}
```

- [ ] **Step 5: Create `buildSrc/src/main/kotlin/kotlin-commons.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain(21)
    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = 60
                    metric = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
                }
                bound {
                    minValue = 55
                    metric = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
                }
            }
        }
    }
}
```

- [ ] **Step 6: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}
```

- [ ] **Step 7: Create module scaffolding**

```bash
for m in commons-kernel-core commons-kernel-errors commons-kernel-result \
  commons-kernel-ddd commons-kernel-time commons-ports-persistence \
  commons-ports-messaging commons-ports-http commons-ports-cache \
  commons-ports-email; do
  mkdir -p "$m/src/main/kotlin" "$m/src/test/kotlin"
  echo 'plugins { id("kotlin-commons") }
group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"' > "$m/build.gradle.kts"
done
mkdir -p commons-bom
echo 'plugins { `java-platform` }
group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"' > commons-bom/build.gradle.kts
```

- [ ] **Step 8: Verify configuration**

```bash
./gradlew tasks --all
```
Expected: lista de tasks sem erros. Se aparecer `Could not resolve`, verifique se o `settings.gradle.kts` lista o módulo correto.

- [ ] **Step 9: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle/ buildSrc/ \
  commons-kernel-core/ commons-kernel-errors/ commons-kernel-result/ \
  commons-kernel-ddd/ commons-kernel-time/ commons-ports-persistence/ \
  commons-ports-messaging/ commons-ports-http/ commons-ports-cache/ \
  commons-ports-email/ commons-bom/
git commit -m "build: initialize Gradle multi-module project with convention plugins"
```

---

### Task 2: commons-kernel-core

**Files:**
- Modify: `commons-kernel-core/build.gradle.kts`
- Create: `commons-kernel-core/src/main/kotlin/com/marcusprado02/commons/kernel/core/strings.kt`
- Create: `commons-kernel-core/src/main/kotlin/com/marcusprado02/commons/kernel/core/uuids.kt`
- Create: `commons-kernel-core/src/main/kotlin/com/marcusprado02/commons/kernel/core/numbers.kt`
- Create: `commons-kernel-core/src/main/kotlin/com/marcusprado02/commons/kernel/core/collections.kt`
- Create: `commons-kernel-core/src/main/kotlin/com/marcusprado02/commons/kernel/core/preconditions.kt`
- Create: `commons-kernel-core/src/test/kotlin/com/marcusprado02/commons/kernel/core/StringsTest.kt`
- Create: `commons-kernel-core/src/test/kotlin/com/marcusprado02/commons/kernel/core/UuidsTest.kt`
- Create: `commons-kernel-core/src/test/kotlin/com/marcusprado02/commons/kernel/core/PreconditionsTest.kt`

- [ ] **Step 1: Update `commons-kernel-core/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
```

- [ ] **Step 2: Write the failing tests**

`commons-kernel-core/src/test/kotlin/com/marcusprado02/commons/kernel/core/StringsTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

class StringsTest : FunSpec({
    test("toSlug converts spaces and special chars") {
        "Hello World!".toSlug() shouldBe "hello-world"
    }

    test("truncate shortens long strings with ellipsis") {
        "Hello World".truncate(8) shouldBe "Hello..."
    }

    test("truncate returns original if within limit") {
        "Hello".truncate(10) shouldBe "Hello"
    }

    test("nullIfBlank returns null for blank string") {
        "   ".nullIfBlank().shouldBeNull()
    }

    test("nullIfBlank returns value for non-blank string") {
        "hello".nullIfBlank().shouldNotBeNull() shouldBe "hello"
    }

    test("nullIfBlank on null returns null") {
        val s: String? = null
        s.nullIfBlank().shouldBeNull()
    }
})
```

`commons-kernel-core/src/test/kotlin/com/marcusprado02/commons/kernel/core/UuidsTest.kt`:
```kotlin
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
```

`commons-kernel-core/src/test/kotlin/com/marcusprado02/commons/kernel/core/PreconditionsTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PreconditionsTest : FunSpec({
    test("requireNotBlank returns value for non-blank string") {
        requireNotBlank("hello") shouldBe "hello"
    }

    test("requireNotBlank throws for blank string") {
        shouldThrow<IllegalArgumentException> { requireNotBlank("  ") }
    }

    test("requirePositive returns value for positive int") {
        requirePositive(5) shouldBe 5
    }

    test("requirePositive throws for zero") {
        shouldThrow<IllegalArgumentException> { requirePositive(0) }
    }
})
```

- [ ] **Step 3: Run tests to verify they fail**

```bash
./gradlew :commons-kernel-core:test
```
Expected: FAIL — `Unresolved reference: toSlug` (e outros símbolos não existem ainda).

- [ ] **Step 4: Create `strings.kt`**

```kotlin
package com.marcusprado02.commons.kernel.core

public fun String.toSlug(): String =
    lowercase()
        .trim()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("[\\s-]+"), "-")
        .trim('-')

public fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    require(maxLength >= ellipsis.length) { "maxLength ($maxLength) must be >= ellipsis.length (${ellipsis.length})" }
    return if (length <= maxLength) this else take(maxLength - ellipsis.length) + ellipsis
}

public fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this

public fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
```

- [ ] **Step 5: Create `uuids.kt`**

```kotlin
package com.marcusprado02.commons.kernel.core

import java.util.UUID

public fun randomUuid(): UUID = UUID.randomUUID()

public fun String.toUuid(): UUID = UUID.fromString(this)

public fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
```

- [ ] **Step 6: Create `numbers.kt`**

```kotlin
package com.marcusprado02.commons.kernel.core

import java.math.BigDecimal
import java.math.RoundingMode

public fun Double.roundTo(scale: Int, mode: RoundingMode = RoundingMode.HALF_UP): Double =
    BigDecimal(this).setScale(scale, mode).toDouble()

public fun Int.isPositive(): Boolean = this > 0
public fun Long.isPositive(): Boolean = this > 0L
public fun Double.isPositive(): Boolean = this > 0.0

public fun Int?.orZero(): Int = this ?: 0
public fun Long?.orZero(): Long = this ?: 0L
public fun Double?.orZero(): Double = this ?: 0.0
```

- [ ] **Step 7: Create `collections.kt`**

```kotlin
package com.marcusprado02.commons.kernel.core

public fun <T> Collection<T>.secondOrNull(): T? = if (size >= 2) elementAt(1) else null

public fun <T> Collection<T>.second(): T =
    secondOrNull() ?: throw NoSuchElementException("Collection has less than 2 elements")

public fun <T> List<T>.updated(index: Int, element: T): List<T> =
    toMutableList().also { it[index] = element }

public fun <K, V> Map<K, V>.mergeWith(other: Map<K, V>, mergeValues: (V, V) -> V): Map<K, V> {
    val result = toMutableMap()
    other.forEach { (k, v) -> result[k] = result[k]?.let { mergeValues(it, v) } ?: v }
    return result
}
```

- [ ] **Step 8: Create `preconditions.kt`**

```kotlin
package com.marcusprado02.commons.kernel.core

public fun requireNotBlank(value: String, lazyMessage: () -> String = { "Value must not be blank" }): String {
    require(value.isNotBlank(), lazyMessage)
    return value
}

public fun requirePositive(value: Int, lazyMessage: () -> String = { "Value must be positive, was $value" }): Int {
    require(value > 0, lazyMessage)
    return value
}

public fun requirePositive(value: Long, lazyMessage: () -> String = { "Value must be positive, was $value" }): Long {
    require(value > 0L, lazyMessage)
    return value
}
```

- [ ] **Step 9: Run tests to verify they pass**

```bash
./gradlew :commons-kernel-core:test
```
Expected: BUILD SUCCESSFUL, all tests PASS.

- [ ] **Step 10: Commit**

```bash
git add commons-kernel-core/
git commit -m "feat(kernel-core): add utility extension functions (strings, uuids, numbers, collections, preconditions)"
```

---

### Task 3: commons-kernel-errors

**Files:**
- Modify: `commons-kernel-errors/build.gradle.kts`
- Create: `src/main/kotlin/.../errors/ErrorCode.kt`
- Create: `src/main/kotlin/.../errors/ErrorCategory.kt`
- Create: `src/main/kotlin/.../errors/Severity.kt`
- Create: `src/main/kotlin/.../errors/ProblemDetail.kt`
- Create: `src/main/kotlin/.../errors/Problem.kt`
- Create: `src/main/kotlin/.../errors/Problems.kt`
- Create: `src/main/kotlin/.../errors/StandardErrorCodes.kt`
- Create: `src/main/kotlin/.../errors/exceptions.kt`
- Create: `src/test/kotlin/.../errors/ProblemTest.kt`
- Create: `src/test/kotlin/.../errors/ExceptionsTest.kt`

- [ ] **Step 1: Update `commons-kernel-errors/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
```

- [ ] **Step 2: Write the failing tests**

`src/test/kotlin/com/marcusprado02/commons/kernel/errors/ProblemTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize

class ProblemTest : FunSpec({
    test("Problem factory creates validation problem") {
        val code = ErrorCode("USER_NOT_FOUND")
        val p = Problems.notFound(code, "User not found")

        p.code shouldBe code
        p.category shouldBe ErrorCategory.NOT_FOUND
        p.severity shouldBe Severity.LOW
        p.message shouldBe "User not found"
        p.details shouldHaveSize 0
    }

    test("Problems.validation attaches details") {
        val detail = ProblemDetail(field = "email", message = "must be valid")
        val p = Problems.validation(ErrorCode("INVALID"), "Invalid data", detail)
        p.details shouldHaveSize 1
        p.details[0].field shouldBe "email"
    }

    test("ErrorCode rejects blank value") {
        shouldThrow<IllegalArgumentException> { ErrorCode("") }
    }

    test("StandardErrorCodes provides NOT_FOUND") {
        StandardErrorCodes.NOT_FOUND.value shouldBe "NOT_FOUND"
    }
})
```

`src/test/kotlin/com/marcusprado02/commons/kernel/errors/ExceptionsTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ExceptionsTest : FunSpec({
    test("NotFoundException carries problem") {
        val problem = Problems.notFound(StandardErrorCodes.NOT_FOUND, "not found")
        val ex = NotFoundException(problem)

        ex.problem shouldBe problem
        ex.message shouldBe "not found"
        ex.shouldBeInstanceOf<DomainException>()
    }

    test("ValidationException is a DomainException") {
        val ex = ValidationException(Problems.validation(ErrorCode("V"), "invalid"))
        ex.shouldBeInstanceOf<DomainException>()
    }
})
```

- [ ] **Step 3: Run tests to verify they fail**

```bash
./gradlew :commons-kernel-errors:test
```
Expected: FAIL — `Unresolved reference: ErrorCode`.

- [ ] **Step 4: Create the error model**

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/ErrorCode.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

@JvmInline
public value class ErrorCode(public val value: String) {
    init { require(value.isNotBlank()) { "ErrorCode must not be blank" } }
    override fun toString(): String = value
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/ErrorCategory.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

public enum class ErrorCategory {
    VALIDATION,
    BUSINESS,
    NOT_FOUND,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,
    TECHNICAL,
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/Severity.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

public enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/ProblemDetail.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

public data class ProblemDetail(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
)
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/Problem.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

import java.time.Instant

public data class Problem(
    val code: ErrorCode,
    val category: ErrorCategory,
    val severity: Severity,
    val message: String,
    val details: List<ProblemDetail> = emptyList(),
    val meta: Map<String, Any> = emptyMap(),
    val timestamp: Instant = Instant.now(),
)
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/Problems.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

public object Problems {
    public fun validation(code: ErrorCode, message: String, vararg details: ProblemDetail): Problem =
        Problem(code, ErrorCategory.VALIDATION, Severity.LOW, message, details.toList())

    public fun business(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.BUSINESS, Severity.MEDIUM, message)

    public fun notFound(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.NOT_FOUND, Severity.LOW, message)

    public fun conflict(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.CONFLICT, Severity.MEDIUM, message)

    public fun unauthorized(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.UNAUTHORIZED, Severity.HIGH, message)

    public fun forbidden(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.FORBIDDEN, Severity.HIGH, message)

    public fun technical(code: ErrorCode, message: String): Problem =
        Problem(code, ErrorCategory.TECHNICAL, Severity.HIGH, message)
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/StandardErrorCodes.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

public object StandardErrorCodes {
    public val VALIDATION_ERROR: ErrorCode = ErrorCode("VALIDATION_ERROR")
    public val BUSINESS_ERROR: ErrorCode = ErrorCode("BUSINESS_ERROR")
    public val NOT_FOUND: ErrorCode = ErrorCode("NOT_FOUND")
    public val CONFLICT: ErrorCode = ErrorCode("CONFLICT")
    public val UNAUTHORIZED: ErrorCode = ErrorCode("UNAUTHORIZED")
    public val FORBIDDEN: ErrorCode = ErrorCode("FORBIDDEN")
    public val TECHNICAL_ERROR: ErrorCode = ErrorCode("TECHNICAL_ERROR")
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/errors/exceptions.kt`:
```kotlin
package com.marcusprado02.commons.kernel.errors

public sealed class DomainException(
    message: String,
    public val problem: Problem,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

public class BusinessException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class ValidationException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class NotFoundException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class ConflictException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class UnauthorizedException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class ForbiddenException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)

public class TechnicalException(problem: Problem, cause: Throwable? = null) :
    DomainException(problem.message, problem, cause)
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-kernel-errors:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-kernel-errors/
git commit -m "feat(kernel-errors): add Problem model, ErrorCategory, Severity, exception hierarchy"
```

---

### Task 4: commons-kernel-result

**Files:**
- Modify: `commons-kernel-result/build.gradle.kts`
- Create: `src/main/kotlin/.../result/Result.kt`
- Create: `src/main/kotlin/.../result/Either.kt`
- Create: `src/main/kotlin/.../result/Option.kt`
- Create: `src/test/kotlin/.../result/ResultTest.kt`
- Create: `src/test/kotlin/.../result/EitherTest.kt`
- Create: `src/test/kotlin/.../result/OptionTest.kt`

- [ ] **Step 1: Update `commons-kernel-result/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(project(":commons-kernel-errors"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

- [ ] **Step 2: Write the failing tests**

`src/test/kotlin/com/marcusprado02/commons/kernel/result/ResultTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ResultTest : FunSpec({
    val problem = Problems.notFound(ErrorCode("NOT_FOUND"), "not found")

    test("ok is ok, not fail") {
        val r = Result.ok("hello")
        r.isOk() shouldBe true
        r.isFail() shouldBe false
        r.getOrNull() shouldBe "hello"
        r.problemOrNull().shouldBeNull()
    }

    test("fail is fail, not ok") {
        val r = Result.fail<String>(problem)
        r.isOk() shouldBe false
        r.isFail() shouldBe true
        r.getOrNull().shouldBeNull()
        r.problemOrNull() shouldBe problem
    }

    test("map transforms ok value") {
        Result.ok("hello").map { it.uppercase() } shouldBe Result.ok("HELLO")
    }

    test("map preserves fail") {
        Result.fail<String>(problem).map { it.uppercase() } shouldBe Result.fail(problem)
    }

    test("flatMap chains ok results") {
        Result.ok(2).flatMap { Result.ok(it * 3) } shouldBe Result.ok(6)
    }

    test("flatMap short-circuits on fail") {
        Result.fail<Int>(problem).flatMap { Result.ok(it * 3) } shouldBe Result.fail(problem)
    }

    test("getOrElse returns value for ok") {
        Result.ok("value").getOrElse("default") shouldBe "value"
    }

    test("getOrElse returns default for fail") {
        Result.fail<String>(problem).getOrElse("default") shouldBe "default"
    }

    test("fold applies onOk for ok") {
        Result.ok(42).fold(onFail = { -1 }, onOk = { it * 2 }) shouldBe 84
    }

    test("fold applies onFail for fail") {
        Result.fail<Int>(problem).fold(onFail = { -1 }, onOk = { it * 2 }) shouldBe -1
    }

    test("peek executes side effect for ok") {
        var called = false
        Result.ok("x").peek { called = true }
        called shouldBe true
    }

    test("peekError executes side effect for fail") {
        var called = false
        Result.fail<String>(problem).peekError { called = true }
        called shouldBe true
    }

    test("mapAsync transforms ok value in suspend context") {
        val r = Result.ok(10).mapAsync { it * 2 }
        r shouldBe Result.ok(20)
    }

    test("mapAsync preserves fail") {
        val r = Result.fail<Int>(problem).mapAsync { it * 2 }
        r shouldBe Result.fail(problem)
    }

    test("mapError transforms problem in fail") {
        val newCode = ErrorCode("NEW_CODE")
        val r = Result.fail<String>(problem).mapError { it.copy(code = newCode) }
        (r as Result.Fail).problem.code shouldBe newCode
    }

    test("recover converts fail to ok") {
        val r = Result.fail<String>(problem).recover { "recovered" }
        r shouldBe Result.ok("recovered")
    }
})
```

`src/test/kotlin/com/marcusprado02/commons/kernel/result/EitherTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull

class EitherTest : FunSpec({
    test("Left isLeft, not isRight") {
        val e = Either.left("error")
        e.isLeft() shouldBe true
        e.isRight() shouldBe false
        e.leftOrNull() shouldBe "error"
        e.rightOrNull().shouldBeNull()
    }

    test("Right isRight, not isLeft") {
        val e = Either.right(42)
        e.isRight() shouldBe true
        e.isLeft() shouldBe false
        e.rightOrNull() shouldBe 42
        e.leftOrNull().shouldBeNull()
    }

    test("mapRight transforms right value") {
        Either.right(10).mapRight { it * 2 } shouldBe Either.right(20)
    }

    test("mapRight preserves left") {
        Either.left("err").mapRight { it: Int -> it * 2 } shouldBe Either.left("err")
    }

    test("fold selects correct branch") {
        Either.left("error").fold(onLeft = { "L:$it" }, onRight = { "R:$it" }) shouldBe "L:error"
        Either.right(99).fold(onLeft = { "L:$it" }, onRight = { "R:$it" }) shouldBe "R:99"
    }
})
```

`src/test/kotlin/com/marcusprado02/commons/kernel/result/OptionTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.result

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class OptionTest : FunSpec({
    test("Some isSome, not isNone") {
        val o = Option.some("value")
        o.isSome() shouldBe true
        o.isNone() shouldBe false
        o.getOrNull() shouldBe "value"
    }

    test("None isNone, not isSome") {
        val o = Option.none<String>()
        o.isNone() shouldBe true
        o.isSome() shouldBe false
        o.getOrNull().shouldBeNull()
    }

    test("Option.of returns Some for non-null") {
        Option.of("hello") shouldBe Option.some("hello")
    }

    test("Option.of returns None for null") {
        Option.of<String>(null) shouldBe Option.none()
    }

    test("toOption extension converts nullable") {
        val s: String? = "hi"
        s.toOption() shouldBe Option.some("hi")
        val n: String? = null
        n.toOption() shouldBe Option.none()
    }

    test("map transforms Some value") {
        Option.some(5).map { it * 2 } shouldBe Option.some(10)
    }

    test("filter keeps Some when predicate is true") {
        Option.some(4).filter { it % 2 == 0 } shouldBe Option.some(4)
    }

    test("filter returns None when predicate is false") {
        Option.some(3).filter { it % 2 == 0 } shouldBe Option.none()
    }
})
```

- [ ] **Step 3: Run tests to verify they fail**

```bash
./gradlew :commons-kernel-result:test
```
Expected: FAIL — `Unresolved reference: Result`.

- [ ] **Step 4: Create `Result.kt`**

`src/main/kotlin/com/marcusprado02/commons/kernel/result/Result.kt`:
```kotlin
package com.marcusprado02.commons.kernel.result

import com.marcusprado02.commons.kernel.errors.Problem

public sealed class Result<out T> {

    public data class Ok<out T>(public val value: T) : Result<T>()
    public data class Fail(public val problem: Problem) : Result<Nothing>()

    public fun isOk(): Boolean = this is Ok
    public fun isFail(): Boolean = this is Fail

    public fun getOrNull(): T? = (this as? Ok)?.value
    public fun problemOrNull(): Problem? = (this as? Fail)?.problem

    public fun getOrElse(default: @UnsafeVariance T): T = if (this is Ok) value else default
    public fun getOrElse(default: () -> @UnsafeVariance T): T = if (this is Ok) value else default()

    public fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Ok -> ok(transform(value))
        is Fail -> this
    }

    public fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Ok -> transform(value)
        is Fail -> this
    }

    public fun mapError(transform: (Problem) -> Problem): Result<T> = when (this) {
        is Ok -> this
        is Fail -> Fail(transform(problem))
    }

    public fun recover(transform: (Problem) -> @UnsafeVariance T): Result<T> = when (this) {
        is Ok -> this
        is Fail -> ok(transform(problem))
    }

    public fun recoverWith(transform: (Problem) -> Result<@UnsafeVariance T>): Result<T> = when (this) {
        is Ok -> this
        is Fail -> transform(problem)
    }

    public fun peek(action: (T) -> Unit): Result<T> {
        if (this is Ok) action(value)
        return this
    }

    public fun peekError(action: (Problem) -> Unit): Result<T> {
        if (this is Fail) action(problem)
        return this
    }

    public fun <R> fold(onFail: (Problem) -> R, onOk: (T) -> R): R = when (this) {
        is Ok -> onOk(value)
        is Fail -> onFail(problem)
    }

    public suspend fun <R> mapAsync(transform: suspend (T) -> R): Result<R> = when (this) {
        is Ok -> ok(transform(value))
        is Fail -> this
    }

    public suspend fun <R> flatMapAsync(transform: suspend (T) -> Result<R>): Result<R> = when (this) {
        is Ok -> transform(value)
        is Fail -> this
    }

    public companion object {
        public fun <T> ok(value: T): Result<T> = Ok(value)
        public fun <T> fail(problem: Problem): Result<T> = Fail(problem)
    }
}
```

- [ ] **Step 5: Create `Either.kt`**

`src/main/kotlin/com/marcusprado02/commons/kernel/result/Either.kt`:
```kotlin
package com.marcusprado02.commons.kernel.result

public sealed class Either<out L, out R> {

    public data class Left<out L>(public val value: L) : Either<L, Nothing>()
    public data class Right<out R>(public val value: R) : Either<Nothing, R>()

    public fun isLeft(): Boolean = this is Left
    public fun isRight(): Boolean = this is Right

    public fun leftOrNull(): L? = (this as? Left)?.value
    public fun rightOrNull(): R? = (this as? Right)?.value

    public fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

    public fun <T> mapRight(transform: (R) -> T): Either<L, T> = when (this) {
        is Left -> this
        is Right -> Right(transform(value))
    }

    public fun <T> mapLeft(transform: (L) -> T): Either<T, R> = when (this) {
        is Left -> Left(transform(value))
        is Right -> this
    }

    public fun <T> flatMapRight(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> = when (this) {
        is Left -> this
        is Right -> transform(value)
    }

    public companion object {
        public fun <L> left(value: L): Either<L, Nothing> = Left(value)
        public fun <R> right(value: R): Either<Nothing, R> = Right(value)
    }
}
```

- [ ] **Step 6: Create `Option.kt`**

`src/main/kotlin/com/marcusprado02/commons/kernel/result/Option.kt`:
```kotlin
package com.marcusprado02.commons.kernel.result

public sealed class Option<out T> {

    public data class Some<out T>(public val value: T) : Option<T>()
    public data object None : Option<Nothing>()

    public fun isSome(): Boolean = this is Some
    public fun isNone(): Boolean = this is None

    public fun getOrNull(): T? = (this as? Some)?.value

    public fun getOrElse(default: @UnsafeVariance T): T = if (this is Some) value else default
    public fun getOrElse(default: () -> @UnsafeVariance T): T = if (this is Some) value else default()

    public fun <R> map(transform: (T) -> R): Option<R> = when (this) {
        is Some -> Some(transform(value))
        is None -> None
    }

    public fun <R> flatMap(transform: (T) -> Option<R>): Option<R> = when (this) {
        is Some -> transform(value)
        is None -> None
    }

    public fun filter(predicate: (T) -> Boolean): Option<T> = when (this) {
        is Some -> if (predicate(value)) this else None
        is None -> None
    }

    public fun ifSome(action: (T) -> Unit): Option<T> {
        if (this is Some) action(value)
        return this
    }

    public companion object {
        public fun <T> some(value: T): Option<T> = Some(value)
        public fun <T> none(): Option<T> = None
        public fun <T> of(value: T?): Option<T> = if (value != null) Some(value) else None
    }
}

public fun <T> T?.toOption(): Option<T> = Option.of(this)
```

- [ ] **Step 7: Run tests to verify they pass**

```bash
./gradlew :commons-kernel-result:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add commons-kernel-result/
git commit -m "feat(kernel-result): add Result<T>, Either<L,R>, Option<T> sealed classes"
```

---

### Task 5: commons-kernel-ddd — Identity types, Audit, Entity

**Files:**
- Modify: `commons-kernel-ddd/build.gradle.kts`
- Create: `src/main/kotlin/.../ddd/identity/TenantId.kt`
- Create: `src/main/kotlin/.../ddd/identity/ActorId.kt`
- Create: `src/main/kotlin/.../ddd/identity/CorrelationId.kt`
- Create: `src/main/kotlin/.../ddd/identity/UuidIdentifier.kt`
- Create: `src/main/kotlin/.../ddd/audit/AuditStamp.kt`
- Create: `src/main/kotlin/.../ddd/audit/DeletionStamp.kt`
- Create: `src/main/kotlin/.../ddd/audit/AuditTrail.kt`
- Create: `src/main/kotlin/.../ddd/entity/EntityVersion.kt`
- Create: `src/main/kotlin/.../ddd/entity/Entity.kt`
- Create: `src/test/kotlin/.../ddd/EntityTest.kt`

- [ ] **Step 1: Update `commons-kernel-ddd/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(project(":commons-kernel-errors"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
```

- [ ] **Step 2: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/kernel/ddd/EntityTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.entity.Entity
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import java.time.Instant

private val TENANT = TenantId("tenant-1")
private val ACTOR = ActorId("actor-1")
private val NOW = Instant.parse("2026-01-01T00:00:00Z")
private val STAMP = AuditStamp(ACTOR, NOW)
private val TRAIL = AuditTrail(STAMP, STAMP)

// Concrete entity for testing
private class TestEntity(id: String, tenantId: TenantId, audit: AuditTrail) :
    Entity<String>(id, tenantId, EntityVersion.INITIAL, audit) {

    fun publicTouch(stamp: AuditStamp) = touch(stamp)
    fun publicSoftDelete(del: DeletionStamp, up: AuditStamp) = softDelete(del, up)
    fun publicRestore(stamp: AuditStamp) = restore(stamp)
}

class EntityTest : FunSpec({
    test("entity starts with initial version and not deleted") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.id shouldBe "id-1"
        entity.tenantId shouldBe TENANT
        entity.version shouldBe EntityVersion.INITIAL
        entity.isDeleted shouldBe false
        entity.deletion.shouldBeNull()
    }

    test("touch increments version") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.publicTouch(STAMP)
        entity.version shouldBe EntityVersion(1L)
    }

    test("softDelete marks entity as deleted") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        val delStamp = DeletionStamp(ACTOR, NOW)
        entity.publicSoftDelete(delStamp, STAMP)
        entity.isDeleted shouldBe true
        entity.deletion shouldBe delStamp
    }

    test("restore unmarks deleted entity") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.publicSoftDelete(DeletionStamp(ACTOR, NOW), STAMP)
        entity.publicRestore(STAMP)
        entity.isDeleted shouldBe false
        entity.deletion.shouldBeNull()
    }

    test("softDelete throws if already deleted") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.publicSoftDelete(DeletionStamp(ACTOR, NOW), STAMP)
        shouldThrow<IllegalStateException> {
            entity.publicSoftDelete(DeletionStamp(ACTOR, NOW), STAMP)
        }
    }

    test("equals is based on id and tenantId") {
        val a = TestEntity("id-1", TENANT, TRAIL)
        val b = TestEntity("id-1", TENANT, TRAIL)
        val c = TestEntity("id-2", TENANT, TRAIL)
        (a == b) shouldBe true
        (a == c) shouldBe false
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-kernel-ddd:test
```
Expected: FAIL — `Unresolved reference: TenantId`.

- [ ] **Step 4: Create identity types**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/identity/TenantId.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.identity

@JvmInline
public value class TenantId(public val value: String) {
    init { require(value.isNotBlank()) { "TenantId must not be blank" } }
    override fun toString(): String = value
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/identity/ActorId.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.identity

@JvmInline
public value class ActorId(public val value: String) {
    init { require(value.isNotBlank()) { "ActorId must not be blank" } }
    override fun toString(): String = value
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/identity/CorrelationId.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.identity

import java.util.UUID

@JvmInline
public value class CorrelationId(public val value: String) {
    override fun toString(): String = value
    public companion object {
        public fun generate(): CorrelationId = CorrelationId(UUID.randomUUID().toString())
    }
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/identity/UuidIdentifier.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.identity

import java.util.UUID

@JvmInline
public value class UuidIdentifier(public val value: UUID) {
    override fun toString(): String = value.toString()
    public companion object {
        public fun generate(): UuidIdentifier = UuidIdentifier(UUID.randomUUID())
        public fun of(value: String): UuidIdentifier = UuidIdentifier(UUID.fromString(value))
    }
}
```

- [ ] **Step 5: Create audit types**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/audit/AuditStamp.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.audit

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import java.time.Instant

public data class AuditStamp(
    val actorId: ActorId,
    val at: Instant,
)
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/audit/DeletionStamp.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.audit

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import java.time.Instant

public data class DeletionStamp(
    val actorId: ActorId,
    val at: Instant,
    val reason: String? = null,
)
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/audit/AuditTrail.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.audit

public data class AuditTrail(
    val created: AuditStamp,
    val updated: AuditStamp,
)
```

- [ ] **Step 6: Create entity types**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/entity/EntityVersion.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.entity

@JvmInline
public value class EntityVersion(public val value: Long) {
    init { require(value >= 0) { "EntityVersion must be non-negative, was $value" } }
    public fun increment(): EntityVersion = EntityVersion(value + 1)
    override fun toString(): String = value.toString()
    public companion object {
        public val INITIAL: EntityVersion = EntityVersion(0L)
    }
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/entity/Entity.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.entity

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public abstract class Entity<I : Any>(
    public val id: I,
    public val tenantId: TenantId,
    initialVersion: EntityVersion = EntityVersion.INITIAL,
    initialAudit: AuditTrail,
    initialDeleted: Boolean = false,
    initialDeletion: DeletionStamp? = null,
) {
    public var version: EntityVersion = initialVersion
        private set
    public var audit: AuditTrail = initialAudit
        private set
    public var isDeleted: Boolean = initialDeleted
        private set
    public var deletion: DeletionStamp? = initialDeletion
        private set

    protected fun touch(updated: AuditStamp) {
        audit = audit.copy(updated = updated)
        version = version.increment()
    }

    protected fun softDelete(stamp: DeletionStamp, updated: AuditStamp) {
        check(!isDeleted) { "Entity ${id} is already deleted" }
        isDeleted = true
        deletion = stamp
        touch(updated)
    }

    protected fun restore(updated: AuditStamp) {
        check(isDeleted) { "Entity ${id} is not deleted" }
        isDeleted = false
        deletion = null
        touch(updated)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*>) return false
        return id == other.id && tenantId == other.tenantId
    }

    override fun hashCode(): Int = 31 * id.hashCode() + tenantId.hashCode()

    override fun toString(): String = "${this::class.simpleName}(id=$id, tenantId=$tenantId)"
}
```

- [ ] **Step 7: Run tests to verify they pass**

```bash
./gradlew :commons-kernel-ddd:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add commons-kernel-ddd/
git commit -m "feat(kernel-ddd): add identity types, audit trail, and Entity base class"
```

---

### Task 6: commons-kernel-ddd — AggregateRoot + Domain Events

**Files:**
- Create: `src/main/kotlin/.../ddd/valueobject/ValueObject.kt`
- Create: `src/main/kotlin/.../ddd/event/EventId.kt`
- Create: `src/main/kotlin/.../ddd/event/EventMetadata.kt`
- Create: `src/main/kotlin/.../ddd/event/DomainEvent.kt`
- Create: `src/main/kotlin/.../ddd/aggregate/AggregateSnapshot.kt`
- Create: `src/main/kotlin/.../ddd/aggregate/AggregateRoot.kt`
- Create: `src/test/kotlin/.../ddd/AggregateRootTest.kt`

- [ ] **Step 1: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/kernel/ddd/AggregateRootTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.aggregate.AggregateRoot
import com.marcusprado02.commons.kernel.ddd.aggregate.AggregateSnapshot
import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.event.DomainEvent
import com.marcusprado02.commons.kernel.ddd.event.EventId
import com.marcusprado02.commons.kernel.ddd.event.EventMetadata
import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Instant

private val TENANT = TenantId("tenant-1")
private val ACTOR = ActorId("actor-1")
private val NOW = Instant.parse("2026-01-01T00:00:00Z")
private val STAMP = AuditStamp(ACTOR, NOW)
private val TRAIL = AuditTrail(STAMP, STAMP)

// Test event
private data class NameChanged(
    override val eventId: EventId,
    override val occurredAt: Instant,
    override val aggregateType: String,
    override val aggregateId: String,
    override val aggregateVersion: EntityVersion,
    override val metadata: EventMetadata,
    val newName: String,
) : DomainEvent

// Concrete aggregate for testing
private class OrderAggregate(id: String, tenantId: TenantId, audit: AuditTrail, var name: String) :
    AggregateRoot<String>(id, tenantId, EntityVersion.INITIAL, audit) {

    fun rename(newName: String, stamp: AuditStamp, meta: EventMetadata) {
        recordChange(stamp, mutation = { name = newName }) { snapshot ->
            NameChanged(
                eventId = EventId.generate(),
                occurredAt = stamp.at,
                aggregateType = "OrderAggregate",
                aggregateId = snapshot.aggregateId,
                aggregateVersion = snapshot.version,
                metadata = meta,
                newName = newName,
            )
        }
    }
}

class AggregateRootTest : FunSpec({
    val meta = EventMetadata(
        correlationId = CorrelationId.generate(),
        tenantId = TENANT,
        actorId = ACTOR,
    )

    test("recordChange mutates state and records event") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        order.rename("Order Renamed", STAMP, meta)

        order.name shouldBe "Order Renamed"
        order.version shouldBe EntityVersion(1L)
        order.peekDomainEvents() shouldHaveSize 1
    }

    test("pullDomainEvents returns and clears events") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        order.rename("Renamed", STAMP, meta)

        val events = order.pullDomainEvents()
        events shouldHaveSize 1
        (events[0] as NameChanged).newName shouldBe "Renamed"
        order.peekDomainEvents() shouldHaveSize 0
    }

    test("snapshot returns current aggregate state") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        val snap = order.snapshot()

        snap.aggregateId shouldBe "o-1"
        snap.tenantId shouldBe TENANT
        snap.version shouldBe EntityVersion.INITIAL
        snap.aggregateType shouldBe "OrderAggregate"
    }

    test("multiple changes accumulate events") {
        val order = OrderAggregate("o-1", TENANT, TRAIL, "Order 1")
        order.rename("Second", STAMP, meta)
        order.rename("Third", STAMP, meta)
        order.peekDomainEvents() shouldHaveSize 2
    }
})
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :commons-kernel-ddd:test --tests "*AggregateRootTest*"
```
Expected: FAIL — `Unresolved reference: AggregateRoot`.

- [ ] **Step 3: Create ValueObject, Event types, AggregateRoot**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/valueobject/ValueObject.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.valueobject

/** Marker interface for all Value Objects in the domain model. */
public interface ValueObject
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/event/EventId.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.event

import java.util.UUID

@JvmInline
public value class EventId(public val value: String) {
    override fun toString(): String = value
    public companion object {
        public fun generate(): EventId = EventId(UUID.randomUUID().toString())
    }
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/event/EventMetadata.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.event

import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public data class EventMetadata(
    val correlationId: CorrelationId,
    val tenantId: TenantId,
    val actorId: ActorId?,
    val extra: Map<String, String> = emptyMap(),
)
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/event/DomainEvent.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.event

import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import java.time.Instant

public interface DomainEvent {
    public val eventId: EventId
    public val occurredAt: Instant
    public val aggregateType: String
    public val aggregateId: String
    public val aggregateVersion: EntityVersion
    public val metadata: EventMetadata
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/aggregate/AggregateSnapshot.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.aggregate

import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public data class AggregateSnapshot<I : Any>(
    val aggregateId: I,
    val tenantId: TenantId,
    val version: EntityVersion,
    val aggregateType: String,
)
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/aggregate/AggregateRoot.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.aggregate

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.entity.Entity
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.event.DomainEvent
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public abstract class AggregateRoot<I : Any>(
    id: I,
    tenantId: TenantId,
    initialVersion: EntityVersion = EntityVersion.INITIAL,
    initialAudit: AuditTrail,
    initialDeleted: Boolean = false,
    initialDeletion: DeletionStamp? = null,
) : Entity<I>(id, tenantId, initialVersion, initialAudit, initialDeleted, initialDeletion) {

    private val _domainEvents = mutableListOf<DomainEvent>()

    protected fun recordChange(
        updated: AuditStamp,
        mutation: () -> Unit,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        mutation()
        touch(updated)
        _domainEvents += event(snapshot())
    }

    protected fun recordSoftDelete(
        stamp: DeletionStamp,
        updated: AuditStamp,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        softDelete(stamp, updated)
        _domainEvents += event(snapshot())
    }

    protected fun recordRestore(
        updated: AuditStamp,
        event: (AggregateSnapshot<I>) -> DomainEvent,
    ) {
        restore(updated)
        _domainEvents += event(snapshot())
    }

    public fun pullDomainEvents(): List<DomainEvent> {
        val events = _domainEvents.toList()
        _domainEvents.clear()
        return events
    }

    public fun peekDomainEvents(): List<DomainEvent> = _domainEvents.toList()

    public fun snapshot(): AggregateSnapshot<I> = AggregateSnapshot(
        aggregateId = id,
        tenantId = tenantId,
        version = version,
        aggregateType = this::class.simpleName ?: "Unknown",
    )
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./gradlew :commons-kernel-ddd:test
```
Expected: BUILD SUCCESSFUL (both EntityTest and AggregateRootTest).

- [ ] **Step 5: Commit**

```bash
git add commons-kernel-ddd/
git commit -m "feat(kernel-ddd): add AggregateRoot with domain events, ValueObject marker, DomainEvent interface"
```

---

### Task 7: commons-kernel-ddd — Specification, Invariants, Context Providers

**Files:**
- Create: `src/main/kotlin/.../ddd/specification/Specification.kt`
- Create: `src/main/kotlin/.../ddd/invariant/Invariant.kt`
- Create: `src/main/kotlin/.../ddd/context/ActorProvider.kt`
- Create: `src/main/kotlin/.../ddd/context/TenantProvider.kt`
- Create: `src/main/kotlin/.../ddd/context/CorrelationProvider.kt`
- Create: `src/test/kotlin/.../ddd/SpecificationTest.kt`
- Create: `src/test/kotlin/.../ddd/InvariantTest.kt`

- [ ] **Step 1: Write the failing tests**

`src/test/kotlin/com/marcusprado02/commons/kernel/ddd/SpecificationTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.specification.Specification
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val isEven = Specification<Int> { it % 2 == 0 }
private val isPositive = Specification<Int> { it > 0 }

class SpecificationTest : FunSpec({
    test("isSatisfiedBy evaluates predicate") {
        isEven.isSatisfiedBy(4) shouldBe true
        isEven.isSatisfiedBy(3) shouldBe false
    }

    test("and combines two specifications") {
        val isEvenAndPositive = isEven and isPositive
        isEvenAndPositive.isSatisfiedBy(4) shouldBe true
        isEvenAndPositive.isSatisfiedBy(-2) shouldBe false
        isEvenAndPositive.isSatisfiedBy(3) shouldBe false
    }

    test("or selects either specification") {
        val isEvenOrPositive = isEven or isPositive
        isEvenOrPositive.isSatisfiedBy(3) shouldBe true   // positive
        isEvenOrPositive.isSatisfiedBy(-2) shouldBe true  // even
        isEvenOrPositive.isSatisfiedBy(-3) shouldBe false // neither
    }

    test("not negates specification") {
        val isOdd = !isEven
        isOdd.isSatisfiedBy(3) shouldBe true
        isOdd.isSatisfiedBy(4) shouldBe false
    }
})
```

`src/test/kotlin/com/marcusprado02/commons/kernel/ddd/InvariantTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.invariant.Invariant
import com.marcusprado02.commons.kernel.errors.ErrorCode
import com.marcusprado02.commons.kernel.errors.Problems
import com.marcusprado02.commons.kernel.errors.ValidationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class InvariantTest : FunSpec({
    test("check does not throw when condition is true") {
        Invariant.check(true) { Problems.validation(ErrorCode("ERR"), "fail") }
        // no exception
    }

    test("check throws ValidationException when condition is false") {
        val ex = shouldThrow<ValidationException> {
            Invariant.check(false) { Problems.validation(ErrorCode("ERR"), "invariant violated") }
        }
        ex.message shouldBe "invariant violated"
    }
})
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./gradlew :commons-kernel-ddd:test --tests "*SpecificationTest*" --tests "*InvariantTest*"
```
Expected: FAIL — `Unresolved reference: Specification`.

- [ ] **Step 3: Create Specification**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/specification/Specification.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.specification

public interface Specification<T> {
    public fun isSatisfiedBy(candidate: T): Boolean

    public infix fun and(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                this@Specification.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate)
        }

    public infix fun or(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                this@Specification.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate)
        }

    public operator fun not(): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(candidate: T): Boolean =
                !this@Specification.isSatisfiedBy(candidate)
        }
}

public fun <T> Specification(predicate: (T) -> Boolean): Specification<T> =
    object : Specification<T> {
        override fun isSatisfiedBy(candidate: T): Boolean = predicate(candidate)
    }
```

- [ ] **Step 4: Create Invariant**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/invariant/Invariant.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.invariant

import com.marcusprado02.commons.kernel.errors.Problem
import com.marcusprado02.commons.kernel.errors.ValidationException

public object Invariant {
    public fun check(condition: Boolean, problem: () -> Problem) {
        if (!condition) throw ValidationException(problem())
    }
}
```

- [ ] **Step 5: Create context providers**

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/context/ActorProvider.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.ActorId

public fun interface ActorProvider {
    public fun currentActor(): ActorId
}

public class FixedActorProvider(private val actorId: ActorId) : ActorProvider {
    override fun currentActor(): ActorId = actorId
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/context/TenantProvider.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.TenantId

public fun interface TenantProvider {
    public fun currentTenant(): TenantId
}

public class FixedTenantProvider(private val tenantId: TenantId) : TenantProvider {
    override fun currentTenant(): TenantId = tenantId
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/ddd/context/CorrelationProvider.kt`:
```kotlin
package com.marcusprado02.commons.kernel.ddd.context

import com.marcusprado02.commons.kernel.ddd.identity.CorrelationId

public fun interface CorrelationProvider {
    public fun currentCorrelation(): CorrelationId
}

public class FixedCorrelationProvider(private val correlationId: CorrelationId) : CorrelationProvider {
    override fun currentCorrelation(): CorrelationId = correlationId
}
```

- [ ] **Step 6: Run all ddd tests to verify they pass**

```bash
./gradlew :commons-kernel-ddd:test
```
Expected: BUILD SUCCESSFUL — all 4 test classes pass.

- [ ] **Step 7: Commit**

```bash
git add commons-kernel-ddd/
git commit -m "feat(kernel-ddd): add Specification, Invariant, context providers (Actor, Tenant, Correlation)"
```

---

### Task 8: commons-kernel-time

**Files:**
- Modify: `commons-kernel-time/build.gradle.kts`
- Create: `src/main/kotlin/.../time/ClockProvider.kt`
- Create: `src/main/kotlin/.../time/SystemClockProvider.kt`
- Create: `src/main/kotlin/.../time/FixedClockProvider.kt`
- Create: `src/main/kotlin/.../time/TimeWindow.kt`
- Create: `src/test/kotlin/.../time/TimeWindowTest.kt`

- [ ] **Step 1: Update `commons-kernel-time/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
```

- [ ] **Step 2: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/kernel/time/TimeWindowTest.kt`:
```kotlin
package com.marcusprado02.commons.kernel.time

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant

class TimeWindowTest : FunSpec({
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
        val w2 = TimeWindow(
            Instant.parse("2026-01-15T00:00:00Z"),
            Instant.parse("2026-02-15T00:00:00Z"),
        )
        w1.overlaps(w2) shouldBe true
    }

    test("overlaps returns false for non-overlapping windows") {
        val w1 = TimeWindow(start, end)
        val w2 = TimeWindow(
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
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-kernel-time:test
```
Expected: FAIL.

- [ ] **Step 4: Create time types**

`src/main/kotlin/com/marcusprado02/commons/kernel/time/ClockProvider.kt`:
```kotlin
package com.marcusprado02.commons.kernel.time

import java.time.Clock
import java.time.Instant

public fun interface ClockProvider {
    public fun clock(): Clock
    public fun now(): Instant = clock().instant()
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/time/SystemClockProvider.kt`:
```kotlin
package com.marcusprado02.commons.kernel.time

import java.time.Clock

public object SystemClockProvider : ClockProvider {
    override fun clock(): Clock = Clock.systemUTC()
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/time/FixedClockProvider.kt`:
```kotlin
package com.marcusprado02.commons.kernel.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

public class FixedClockProvider(
    private val fixedInstant: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) : ClockProvider {
    override fun clock(): Clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
}
```

`src/main/kotlin/com/marcusprado02/commons/kernel/time/TimeWindow.kt`:
```kotlin
package com.marcusprado02.commons.kernel.time

import java.time.Duration
import java.time.Instant

public data class TimeWindow(
    val start: Instant,
    val end: Instant,
) {
    init { require(!end.isBefore(start)) { "TimeWindow end must not be before start" } }

    public fun contains(instant: Instant): Boolean =
        !instant.isBefore(start) && !instant.isAfter(end)

    public fun duration(): Duration = Duration.between(start, end)

    public fun overlaps(other: TimeWindow): Boolean =
        start.isBefore(other.end) && end.isAfter(other.start)

    public companion object {
        public fun of(start: Instant, duration: Duration): TimeWindow =
            TimeWindow(start, start.plus(duration))
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-kernel-time:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-kernel-time/
git commit -m "feat(kernel-time): add ClockProvider, SystemClockProvider, FixedClockProvider, TimeWindow"
```

---

### Task 9: commons-ports-persistence

**Files:**
- Modify: `commons-ports-persistence/build.gradle.kts`
- Create: `src/main/kotlin/.../ports/persistence/Repository.kt`
- Create: `src/main/kotlin/.../ports/persistence/PageableRepository.kt`
- Create: `src/main/kotlin/.../ports/persistence/PageRequest.kt`
- Create: `src/main/kotlin/.../ports/persistence/PageResult.kt`
- Create: `src/main/kotlin/.../ports/persistence/QuerySpecification.kt`
- Create: `src/main/kotlin/.../ports/persistence/PersistenceException.kt`

- [ ] **Step 1: Update `commons-ports-persistence/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(project(":commons-kernel-ddd"))
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 2: Write the failing test (compilation + contract test)**

`src/test/kotlin/com/marcusprado02/commons/ports/persistence/RepositoryContractTest.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull

// In-memory implementation to validate the contract compiles correctly
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
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-ports-persistence:test
```
Expected: FAIL — `Unresolved reference: Repository`.

- [ ] **Step 4: Create the persistence port files**

`src/main/kotlin/com/marcusprado02/commons/ports/persistence/Repository.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

public interface Repository<E : Any, I : Any> {
    public suspend fun findById(id: I): E?
    public suspend fun save(entity: E): E
    public suspend fun delete(entity: E)
    public suspend fun deleteById(id: I)
    public suspend fun existsById(id: I): Boolean
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/persistence/PageableRepository.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

public interface PageableRepository<E : Any, I : Any> : Repository<E, I> {
    public suspend fun findAll(request: PageRequest): PageResult<E>
    public suspend fun count(): Long
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/persistence/PageRequest.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

public data class PageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sort: List<SortField> = emptyList(),
) {
    init {
        require(page >= 0) { "Page must be non-negative, was $page" }
        require(size > 0) { "Size must be positive, was $size" }
    }
}

public data class SortField(
    val field: String,
    val direction: SortDirection = SortDirection.ASC,
)

public enum class SortDirection { ASC, DESC }
```

`src/main/kotlin/com/marcusprado02/commons/ports/persistence/PageResult.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

public data class PageResult<E>(
    val content: List<E>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    public val totalPages: Int = if (size == 0) 0 else ((totalElements + size - 1) / size).toInt()
    public val isFirst: Boolean = page == 0
    public val isLast: Boolean = page >= totalPages - 1
    public val isEmpty: Boolean = content.isEmpty()
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/persistence/QuerySpecification.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

/** Marker interface for adapter-specific query specifications (JPA Criteria, MongoDB filters, etc.). */
public interface QuerySpecification<E : Any>
```

`src/main/kotlin/com/marcusprado02/commons/ports/persistence/PersistenceException.kt`:
```kotlin
package com.marcusprado02.commons.ports.persistence

public open class PersistenceException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

public class EntityNotFoundException(message: String) : PersistenceException(message)

public class OptimisticLockException(message: String, cause: Throwable? = null) :
    PersistenceException(message, cause)
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-ports-persistence:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-ports-persistence/
git commit -m "feat(ports-persistence): add Repository, PageableRepository, PageRequest, PageResult ports"
```

---

### Task 10: commons-ports-messaging

**Files:**
- Modify: `commons-ports-messaging/build.gradle.kts`
- Create: `src/main/kotlin/.../ports/messaging/MessageId.kt`
- Create: `src/main/kotlin/.../ports/messaging/TopicName.kt`
- Create: `src/main/kotlin/.../ports/messaging/MessageHeaders.kt`
- Create: `src/main/kotlin/.../ports/messaging/MessageEnvelope.kt`
- Create: `src/main/kotlin/.../ports/messaging/MessagePublisherPort.kt`
- Create: `src/main/kotlin/.../ports/messaging/MessageConsumerPort.kt`

- [ ] **Step 1: Update `commons-ports-messaging/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 2: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/ports/messaging/MessagingPortTest.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.Instant

class MessagingPortTest : FunSpec({
    test("MessageId generates unique values") {
        val id1 = MessageId.generate()
        val id2 = MessageId.generate()
        (id1 == id2) shouldBe false
    }

    test("TopicName rejects blank value") {
        val ex = runCatching { TopicName("") }.exceptionOrNull()
        (ex is IllegalArgumentException) shouldBe true
    }

    test("publish is called with correct envelope") {
        val publisher = mockk<MessagePublisherPort>(relaxed = true)
        val envelope = MessageEnvelope(
            topic = TopicName("orders"),
            body = "payload".toByteArray(),
            headers = MessageHeaders(
                messageId = MessageId.generate(),
                timestamp = Instant.now(),
            ),
        )

        publisher.publish(envelope)
        coVerify(exactly = 1) { publisher.publish(envelope) }
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-ports-messaging:test
```
Expected: FAIL.

- [ ] **Step 4: Create the messaging port files**

`src/main/kotlin/com/marcusprado02/commons/ports/messaging/MessageId.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

import java.util.UUID

@JvmInline
public value class MessageId(public val value: String) {
    override fun toString(): String = value
    public companion object {
        public fun generate(): MessageId = MessageId(UUID.randomUUID().toString())
    }
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/messaging/TopicName.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

@JvmInline
public value class TopicName(public val value: String) {
    init { require(value.isNotBlank()) { "TopicName must not be blank" } }
    override fun toString(): String = value
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/messaging/MessageHeaders.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

import java.time.Instant

public data class MessageHeaders(
    val messageId: MessageId,
    val timestamp: Instant,
    val correlationId: String? = null,
    val tenantId: String? = null,
    val extra: Map<String, String> = emptyMap(),
)
```

`src/main/kotlin/com/marcusprado02/commons/ports/messaging/MessageEnvelope.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

public data class MessageEnvelope<T>(
    val topic: TopicName,
    val body: T,
    val headers: MessageHeaders,
)
```

`src/main/kotlin/com/marcusprado02/commons/ports/messaging/MessagePublisherPort.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

public interface MessagePublisherPort {
    public suspend fun publish(envelope: MessageEnvelope<*>)
    public suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>)
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/messaging/MessageConsumerPort.kt`:
```kotlin
package com.marcusprado02.commons.ports.messaging

@JvmInline
public value class ConsumerGroup(public val value: String)

public interface MessageConsumerPort {
    public suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>?
    public suspend fun acknowledge(messageId: MessageId)
    public suspend fun nack(messageId: MessageId)
}
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-ports-messaging:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-ports-messaging/
git commit -m "feat(ports-messaging): add MessagePublisherPort, MessageConsumerPort, MessageEnvelope"
```

---

### Task 11: commons-ports-http

**Files:**
- Modify: `commons-ports-http/build.gradle.kts`
- Create: `src/main/kotlin/.../ports/http/HttpMethod.kt`
- Create: `src/main/kotlin/.../ports/http/HttpBody.kt`
- Create: `src/main/kotlin/.../ports/http/HttpRequest.kt`
- Create: `src/main/kotlin/.../ports/http/HttpResponse.kt`
- Create: `src/main/kotlin/.../ports/http/HttpClientPort.kt`

- [ ] **Step 1: Update `commons-ports-http/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 2: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/ports/http/HttpPortTest.kt`:
```kotlin
package com.marcusprado02.commons.ports.http

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.net.URI

class HttpPortTest : FunSpec({
    test("HttpResponse.isSuccessful for 200 range") {
        val r = HttpResponse<ByteArray>(200, emptyMap(), null)
        r.isSuccessful shouldBe true
        r.isClientError shouldBe false
        r.isServerError shouldBe false
    }

    test("HttpResponse.isClientError for 400 range") {
        val r = HttpResponse<ByteArray>(404, emptyMap(), null)
        r.isClientError shouldBe true
        r.isSuccessful shouldBe false
    }

    test("HttpClientPort.execute returns mocked response") {
        val client = mockk<HttpClientPort>()
        val request = HttpRequest(URI.create("https://example.com"), HttpMethod.GET)
        val response = HttpResponse(200, emptyMap(), "hello".toByteArray())

        coEvery { client.execute(request) } returns response

        val result = client.execute(request)
        result.statusCode shouldBe 200
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-ports-http:test
```
Expected: FAIL.

- [ ] **Step 4: Create the HTTP port files**

`src/main/kotlin/com/marcusprado02/commons/ports/http/HttpMethod.kt`:
```kotlin
package com.marcusprado02.commons.ports.http

public enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/http/HttpBody.kt`:
```kotlin
package com.marcusprado02.commons.ports.http

public sealed class HttpBody {
    public data class Bytes(val content: ByteArray, val contentType: String) : HttpBody()
    public data class FormUrlEncoded(val params: Map<String, String>) : HttpBody()
    public data class Multipart(val parts: List<MultipartPart>) : HttpBody()
}

public data class MultipartPart(
    val name: String,
    val content: ByteArray,
    val contentType: String,
    val filename: String? = null,
)
```

`src/main/kotlin/com/marcusprado02/commons/ports/http/HttpRequest.kt`:
```kotlin
package com.marcusprado02.commons.ports.http

import java.net.URI
import java.time.Duration

public data class HttpRequest(
    val uri: URI,
    val method: HttpMethod,
    val headers: Map<String, String> = emptyMap(),
    val body: HttpBody? = null,
    val timeout: Duration? = null,
    val name: String? = null,
)
```

`src/main/kotlin/com/marcusprado02/commons/ports/http/HttpResponse.kt`:
```kotlin
package com.marcusprado02.commons.ports.http

public data class HttpResponse<T>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: T?,
) {
    public val isSuccessful: Boolean get() = statusCode in 200..299
    public val isClientError: Boolean get() = statusCode in 400..499
    public val isServerError: Boolean get() = statusCode in 500..599
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/http/HttpClientPort.kt`:
```kotlin
package com.marcusprado02.commons.ports.http

public interface HttpClientPort {
    public suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>
    public suspend fun <T> execute(request: HttpRequest, mapper: (ByteArray) -> T): HttpResponse<T>
}
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-ports-http:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-ports-http/
git commit -m "feat(ports-http): add HttpClientPort with suspend API, HttpRequest, HttpResponse, HttpMethod"
```

---

### Task 12: commons-ports-cache

**Files:**
- Modify: `commons-ports-cache/build.gradle.kts`
- Create: `src/main/kotlin/.../ports/cache/CacheKey.kt`
- Create: `src/main/kotlin/.../ports/cache/CachePort.kt`

- [ ] **Step 1: Update `commons-ports-cache/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 2: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/ports/cache/CachePortTest.kt`:
```kotlin
package com.marcusprado02.commons.ports.cache

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import java.time.Duration

// In-memory implementation for contract verification
private class InMemoryCache : CachePort {
    private val store = mutableMapOf<String, Any>()

    override suspend fun <T : Any> get(key: CacheKey, type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return store[key.value] as? T
    }

    override suspend fun <T : Any> put(key: CacheKey, value: T, ttl: Duration?) {
        store[key.value] = value
    }

    override suspend fun remove(key: CacheKey) { store.remove(key.value) }
    override suspend fun clear() { store.clear() }
    override suspend fun exists(key: CacheKey): Boolean = store.containsKey(key.value)
}

class CachePortTest : FunSpec({
    test("put and get round-trip") {
        val cache = InMemoryCache()
        cache.put(CacheKey("k1"), "value1")
        cache.get<String>(CacheKey("k1")) shouldBe "value1"
    }

    test("remove deletes entry") {
        val cache = InMemoryCache()
        cache.put(CacheKey("k1"), "v1")
        cache.remove(CacheKey("k1"))
        cache.get<String>(CacheKey("k1")).shouldBeNull()
    }

    test("exists returns false for missing key") {
        val cache = InMemoryCache()
        cache.exists(CacheKey("missing")) shouldBe false
    }

    test("CacheKey rejects blank value") {
        val ex = runCatching { CacheKey("") }.exceptionOrNull()
        (ex is IllegalArgumentException) shouldBe true
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-ports-cache:test
```
Expected: FAIL.

- [ ] **Step 4: Create cache port files**

`src/main/kotlin/com/marcusprado02/commons/ports/cache/CacheKey.kt`:
```kotlin
package com.marcusprado02.commons.ports.cache

@JvmInline
public value class CacheKey(public val value: String) {
    init { require(value.isNotBlank()) { "CacheKey must not be blank" } }
    override fun toString(): String = value
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/cache/CachePort.kt`:
```kotlin
package com.marcusprado02.commons.ports.cache

import java.time.Duration

public interface CachePort {
    public suspend fun <T : Any> get(key: CacheKey, type: Class<T>): T?
    public suspend fun <T : Any> put(key: CacheKey, value: T, ttl: Duration? = null)
    public suspend fun remove(key: CacheKey)
    public suspend fun clear()
    public suspend fun exists(key: CacheKey): Boolean
}

/** Reified helper to avoid passing Class<T> explicitly. */
public suspend inline fun <reified T : Any> CachePort.get(key: CacheKey): T? = get(key, T::class.java)
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-ports-cache:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-ports-cache/
git commit -m "feat(ports-cache): add CachePort with suspend API and reified get helper"
```

---

### Task 13: commons-ports-email

**Files:**
- Modify: `commons-ports-email/build.gradle.kts`
- Create: `src/main/kotlin/.../ports/email/EmailAddress.kt`
- Create: `src/main/kotlin/.../ports/email/EmailContent.kt`
- Create: `src/main/kotlin/.../ports/email/EmailAttachment.kt`
- Create: `src/main/kotlin/.../ports/email/Email.kt`
- Create: `src/main/kotlin/.../ports/email/EmailPort.kt`

- [ ] **Step 1: Update `commons-ports-email/build.gradle.kts`**

```kotlin
plugins {
    id("kotlin-commons")
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 2: Write the failing test**

`src/test/kotlin/com/marcusprado02/commons/ports/email/EmailPortTest.kt`:
```kotlin
package com.marcusprado02.commons.ports.email

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk

class EmailPortTest : FunSpec({
    test("EmailAddress validates @ symbol") {
        val ea = EmailAddress("user@example.com", "User")
        ea.address shouldBe "user@example.com"
        ea.displayName shouldBe "User"
    }

    test("EmailAddress rejects invalid address") {
        shouldThrow<IllegalArgumentException> { EmailAddress("not-an-email") }
    }

    test("EmailContent requires at least html or plain") {
        shouldThrow<IllegalArgumentException> { EmailContent(html = null, plain = null) }
    }

    test("Email requires at least one recipient") {
        shouldThrow<IllegalArgumentException> {
            Email(
                from = EmailAddress("from@example.com"),
                to = emptyList(),
                subject = "Test",
                content = EmailContent(plain = "body"),
            )
        }
    }

    test("EmailPort.send is called with correct email") {
        val port = mockk<EmailPort>(relaxed = true)
        val email = Email(
            from = EmailAddress("from@example.com"),
            to = listOf(EmailAddress("to@example.com")),
            subject = "Hello",
            content = EmailContent(plain = "Hi there"),
        )
        port.send(email)
        coVerify(exactly = 1) { port.send(email) }
    }
})
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./gradlew :commons-ports-email:test
```
Expected: FAIL.

- [ ] **Step 4: Create email port files**

`src/main/kotlin/com/marcusprado02/commons/ports/email/EmailAddress.kt`:
```kotlin
package com.marcusprado02.commons.ports.email

public data class EmailAddress(
    val address: String,
    val displayName: String? = null,
) {
    init { require(address.contains("@")) { "Invalid email address: $address" } }
    override fun toString(): String = if (displayName != null) "$displayName <$address>" else address
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/email/EmailContent.kt`:
```kotlin
package com.marcusprado02.commons.ports.email

public data class EmailContent(
    val html: String? = null,
    val plain: String? = null,
) {
    init { require(html != null || plain != null) { "Email must have at least html or plain content" } }
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/email/EmailAttachment.kt`:
```kotlin
package com.marcusprado02.commons.ports.email

public data class EmailAttachment(
    val filename: String,
    val content: ByteArray,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmailAttachment) return false
        return filename == other.filename && content.contentEquals(other.content) && mimeType == other.mimeType
    }
    override fun hashCode(): Int = 31 * filename.hashCode() + content.contentHashCode() + mimeType.hashCode()
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/email/Email.kt`:
```kotlin
package com.marcusprado02.commons.ports.email

public data class Email(
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val subject: String,
    val content: EmailContent,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val replyTo: EmailAddress? = null,
    val attachments: List<EmailAttachment> = emptyList(),
) {
    init {
        require(to.isNotEmpty()) { "Email must have at least one recipient" }
        require(subject.isNotBlank()) { "Email subject must not be blank" }
    }
}
```

`src/main/kotlin/com/marcusprado02/commons/ports/email/EmailPort.kt`:
```kotlin
package com.marcusprado02.commons.ports.email

public interface EmailPort {
    public suspend fun send(email: Email)
    public suspend fun sendBatch(emails: List<Email>)
}
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :commons-ports-email:test
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add commons-ports-email/
git commit -m "feat(ports-email): add EmailPort with suspend API, Email, EmailAddress, EmailContent"
```

---

### Task 14: commons-bom

**Files:**
- Modify: `commons-bom/build.gradle.kts`

- [ ] **Step 1: Update `commons-bom/build.gradle.kts`**

```kotlin
plugins {
    `java-platform`
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

javaPlatform {
    allowDependencies()
}

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
    }
}
```

- [ ] **Step 2: Run full build to verify all modules compile and tests pass**

```bash
./gradlew build
```
Expected: BUILD SUCCESSFUL — all 10 modules compile, all tests pass. If you see `koverVerify` failures, check individual module coverage with `./gradlew :commons-kernel-core:koverHtmlReport`.

- [ ] **Step 3: Commit**

```bash
git add commons-bom/
git commit -m "feat(bom): add Bill of Materials for Fase 1 modules"
```

---

## Verification Checklist

After all tasks are complete, run:

```bash
# Full build + test + coverage
./gradlew build

# Confirm all modules listed
./gradlew projects

# List tests per module
./gradlew test --tests "*Test*"
```

Expected final state:
- 11 modules declared in `settings.gradle.kts`
- All tests pass
- Coverage ≥ 60% line / 55% branch per module
- No compiler warnings (allWarningsAsErrors = true)
