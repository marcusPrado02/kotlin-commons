# Getting Started

kotlin-commons is a modular set of Kotlin libraries for production-grade JVM backend services. It is organised in three layers: **kernel** (pure domain primitives with no framework dependencies), **ports** (interface contracts for infrastructure), and **adapters** (implementations backed by popular libraries).

The library exists because common cross-cutting concerns — typed error handling, DDD building blocks, and infrastructure abstractions — should not be reinvented in every service. kotlin-commons provides these as small, focused modules that can be adopted individually or together.

---

## Prerequisites

- JVM 21+
- Kotlin 2.1.0+
- Gradle 8+ or Maven 3.9+

---

## Installation

### Gradle (recommended — use the BOM)

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))

    // pick only the modules you need
    implementation("io.github.marcusprado02.commons:commons-kernel-result")
    implementation("io.github.marcusprado02.commons:commons-kernel-errors")
    implementation("io.github.marcusprado02.commons:commons-ports-cache")
    implementation("io.github.marcusprado02.commons:commons-adapters-cache-redis")

    // test helpers
    testImplementation("io.github.marcusprado02.commons:commons-testkit-testcontainers")
}
```

Replace `VERSION` with the latest badge version shown in the [README](../../README.md).

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.marcusprado02.commons</groupId>
      <artifactId>commons-bom</artifactId>
      <version>VERSION</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.marcusprado02.commons</groupId>
    <artifactId>commons-kernel-result</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.marcusprado02.commons</groupId>
    <artifactId>commons-kernel-errors</artifactId>
  </dependency>
</dependencies>
```

---

## First end-to-end example

This example creates a simple user lookup that combines `Result` for typed error propagation and `Problem` for structured error responses.

```kotlin
import com.marcusprado02.commons.kernel.errors.*
import com.marcusprado02.commons.kernel.result.*

// Domain model
data class UserId(val value: String)
data class User(val id: UserId, val email: String)

// Repository returns Result instead of throwing
interface UserRepository {
    fun findById(id: UserId): Result<Problem, User>
}

// Service layer — errors flow as values, no try/catch
class UserService(private val repo: UserRepository) {
    fun getUser(id: UserId): Result<Problem, User> =
        repo.findById(id)

    fun getUserEmail(id: UserId): Result<Problem, String> =
        getUser(id).map { it.email }
}

// Usage — fold at the boundary
fun handleRequest(id: String): String {
    val service = UserService(InMemoryUserRepository())
    return service.getUser(UserId(id)).fold(
        onLeft  = { problem -> "Error ${problem.category}: ${problem.message}" },
        onRight = { user    -> "Found: ${user.email}" }
    )
}
```

`Result<E, A>` is `Left<E>` (error) or `Right<A>` (success). `fold` forces you to handle both cases at the call site.

---

## Next steps

- [kernel.md](kernel.md) — Result, Problem, DDD building blocks, time utilities
- [ports.md](ports.md) — cache, persistence, messaging, HTTP, email port contracts
- [adapters.md](adapters.md) — Redis, JPA, Kafka, OkHttp, SMTP adapter wiring
