# kotlin-commons

[![CI](https://github.com/marcusPrado02/kotlin-commons/actions/workflows/ci.yml/badge.svg)](https://github.com/marcusPrado02/kotlin-commons/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.marcusprado02.commons/commons-bom)](https://central.sonatype.com/artifact/io.github.marcusprado02.commons/commons-bom)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0%2B-blue)](https://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-21%2B-orange)](https://openjdk.org)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)

A set of opinionated Kotlin libraries for building production-grade backend services on the JVM. Provides kernel primitives (Result, Problem, DDD building blocks, time), port interfaces (cache, persistence, messaging, HTTP, email), and ready-to-use adapter implementations backed by Redis, PostgreSQL/JPA, Kafka, OkHttp, and SMTP.

**Language / Idioma / Idioma:** &nbsp; 🇺🇸 [English](docs/en/getting-started.md) &nbsp;|&nbsp; 🇧🇷 [Português](docs/pt/getting-started.md) &nbsp;|&nbsp; 🇪🇸 [Español](docs/es/getting-started.md)

---

## Quick taste

### Kernel — typed errors without exceptions

```kotlin
val result: Result<Problem, User> = userRepository.findById(id)
    .toResult { Problems.notFound(StandardErrorCodes.NOT_FOUND, "User $id not found") }

result.fold(
    onLeft  = { problem -> ResponseEntity.status(problem.category.httpStatus).body(problem) },
    onRight = { user    -> ResponseEntity.ok(user) }
)
```

### Ports — depend on interfaces, not libraries

```kotlin
class UserService(private val cache: CachePort) {
    suspend fun findCached(id: UserId): User? =
        cache.getOrPut("user:${id.value}", ttl = 5.minutes) { fetchFromDb(id) }
}
```

### Adapters — wire in one line

```kotlin
@Bean
fun cachePort(redis: RedisTemplate<String, ByteArray>): CachePort =
    RedisCacheAdapter(redis)
```

---

See the [getting started guide](docs/en/getting-started.md) for installation and a full end-to-end example.
