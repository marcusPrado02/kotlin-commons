# kotlin-commons — Fase 2: Adapters Design Spec

**Date:** 2026-04-16
**Status:** Approved
**Author:** Marcus Prado Silva

---

## Goal

Implement 6 new modules: a shared test-infrastructure module (`commons-testkit-testcontainers`) and 5 concrete adapter modules that implement the port interfaces defined in Fase 1. Add a new `kotlin-commons-spring.gradle.kts` convention plugin for Spring-based adapters.

---

## Module Structure

```
kotlin-commons/
├── buildSrc/
│   └── src/main/kotlin/
│       ├── kotlin-commons.gradle.kts           (existing)
│       └── kotlin-commons-spring.gradle.kts    (new)
│
├── commons-testkit-testcontainers/             (new)
├── commons-adapters-persistence-jpa/           (new — Spring Data JPA)
├── commons-adapters-cache-redis/               (new — Spring Data Redis)
├── commons-adapters-messaging-kafka/           (new — kafka-clients, no Spring)
├── commons-adapters-http-okhttp/               (new — OkHttp, no Spring)
├── commons-adapters-email-smtp/                (new — JavaMail, no Spring)
└── commons-bom/                                (updated — adds 6 new constraints)
```

---

## Architecture

### Dependency rules

```
commons-kernel-*          ← zero external deps
      ↑
commons-ports-*           ← kernel + kotlinx-coroutines-core
      ↑
commons-adapters-*        ← ports + technology lib (JPA/Redis/Kafka/OkHttp/JavaMail)
      ↑
commons-testkit-*         ← test-only infra (Testcontainers, GreenMail)
```

Adapters declare their port as `api(project(":commons-ports-*"))` so consumers get the port interface transitively.

Spring-based adapters (`-jpa`, `-redis`) apply `kotlin-commons-spring.gradle.kts`.
Framework-agnostic adapters (`-kafka`, `-okhttp`, `-smtp`) apply `kotlin-commons.gradle.kts`.

---

## Convention Plugin — `kotlin-commons-spring.gradle.kts`

Extends the base plugin with Spring Boot support:

- Applies `kotlin-commons.gradle.kts`
- Applies `kotlin("plugin.spring")` — generates `open` on Spring-annotated classes automatically
- Imports `spring-boot-dependencies` BOM via `dependencyManagement` (version from version catalog)
- Adds to `testImplementation`: `spring-boot-starter-test`, `mockk`

```kotlin
// buildSrc/build.gradle.kts must add to plugins block:
//   id("io.spring.dependency-management") version "<version>"
// so it's available as a convention plugin dependency.

plugins {
    id("kotlin-commons")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
    }
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation(libs.mockk)
}
```

---

## Module: `commons-testkit-testcontainers`

**Purpose:** Shared Testcontainers singletons used by adapter test suites. Code lives in `main` source set so other modules can declare `testImplementation(project(":commons-testkit-testcontainers"))`.

**Convention plugin:** Plain `kotlin("jvm")` only — no Kover, ktlint, or detekt (test-infra module, not production code).

### Container singletons

Each object starts its container lazily on first access and reuses it for the entire JVM lifetime (one container per Gradle test task execution):

```kotlin
object PostgresContainers {
    val instance: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:16-alpine").also { it.start() }
    }
}

object RedisContainers {
    val instance: GenericContainer<*> by lazy {
        GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)
            .also { it.start() }
    }
}

object KafkaContainers {
    val instance: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))
            .also { it.start() }
    }
}

object GreenMailContainers {
    val instance: GreenMailContainer by lazy {
        GreenMailContainer().also { it.start() }
    }
}
```

### Dependencies

```toml
# gradle/libs.versions.toml additions
testcontainers = "1.20.4"
greenmail = "2.1.2"

[libraries]
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }
testcontainers-postgresql = { module = "org.testcontainers:postgresql" }
testcontainers-kafka = { module = "org.testcontainers:kafka" }
testcontainers-generic = { module = "org.testcontainers:testcontainers" }
greenmail-testcontainers = { module = "com.icegreen:greenmail-testcontainers", version.ref = "greenmail" }
```

---

## Module: `commons-adapters-persistence-jpa`

**Port implemented:** `Repository<E, I>`, `PageableRepository<E, I>` from `commons-ports-persistence`

**Convention plugin:** `kotlin-commons-spring.gradle.kts`

### Core classes

**`JpaRepositoryAdapter<E, I>`** — abstract base for single-entity repositories:

```kotlin
abstract class JpaRepositoryAdapter<E : Any, I : Any>(
    private val jpa: JpaRepository<E, I>,
) : Repository<E, I> {
    override suspend fun findById(id: I): E? =
        withContext(Dispatchers.IO) {
            try { jpa.findById(id).orElse(null) }
            catch (ex: DataAccessException) { throw PersistenceException("findById failed", ex) }
        }
    override suspend fun save(entity: E): E =
        withContext(Dispatchers.IO) {
            try { jpa.save(entity) }
            catch (ex: DataAccessException) { throw PersistenceException("save failed", ex) }
        }
    override suspend fun delete(entity: E): Unit =
        withContext(Dispatchers.IO) {
            try { jpa.delete(entity) }
            catch (ex: DataAccessException) { throw PersistenceException("delete failed", ex) }
        }
    override suspend fun deleteById(id: I): Unit =
        withContext(Dispatchers.IO) {
            try { jpa.deleteById(id) }
            catch (ex: DataAccessException) { throw PersistenceException("deleteById failed", ex) }
        }
}
```

**`JpaPageableRepositoryAdapter<E, I>`** — extends above with pagination and spec queries:

```kotlin
abstract class JpaPageableRepositoryAdapter<E : Any, I : Any>(
    private val jpa: JpaRepository<E, I>,
) : JpaRepositoryAdapter<E, I>(jpa), PageableRepository<E, I> {
    override suspend fun findAll(page: PageRequest): PageResult<E> =
        withContext(Dispatchers.IO) {
            try { jpa.findAll(page.toSpringPageable()).toPageResult() }
            catch (ex: DataAccessException) { throw PersistenceException("findAll failed", ex) }
        }

    override suspend fun findAll(spec: QuerySpecification<E>, page: PageRequest): PageResult<E> =
        withContext(Dispatchers.IO) {
            val executor = jpa as? JpaSpecificationExecutor<E>
                ?: error("JPA repository must implement JpaSpecificationExecutor for spec queries")
            try { executor.findAll(spec.toJpaSpecification(), page.toSpringPageable()).toPageResult() }
            catch (ex: DataAccessException) { throw PersistenceException("findAll(spec) failed", ex) }
        }
}
```

### Internal converters (not public)

- `PageRequest.toSpringPageable(): Pageable` — maps page/size to `PageRequest.of(page, size)`
- `org.springframework.data.domain.Page<E>.toPageResult(): PageResult<E>` — maps content, page, size, totalElements
- `QuerySpecification<E>.toJpaSpecification(): Specification<E>` — wraps the functional interface

### Dependencies

```kotlin
api(project(":commons-ports-persistence"))
api("org.springframework.boot:spring-boot-starter-data-jpa")
api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
testImplementation(project(":commons-testkit-testcontainers"))
testImplementation(libs.testcontainers.postgresql)
testImplementation("org.springframework.boot:spring-boot-starter-test")
runtimeOnly("org.postgresql:postgresql")
```

### Testing

`@DataJpaTest` with PostgreSQL Testcontainers singleton. A simple `TestEntity` + `TestJpaRepository` defined in the test source set exercises all adapter methods — no application-level entities needed in this module.

---

## Module: `commons-adapters-cache-redis`

**Port implemented:** `CachePort` from `commons-ports-cache`

**Convention plugin:** `kotlin-commons-spring.gradle.kts`

### Core class

```kotlin
class RedisCacheAdapter(
    private val redis: RedisTemplate<String, ByteArray>,
    private val objectMapper: ObjectMapper,
) : CachePort {
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> get(key: CacheKey): T? =
        withContext(Dispatchers.IO) {
            try {
                // ObjectMapper must have default typing enabled (see note below)
                redis.opsForValue().get(key.value)
                    ?.let { objectMapper.readValue(it, Any::class.java) as T? }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis get failed", ex)
            }
        }

    override suspend fun <T : Any> put(key: CacheKey, value: T, ttl: Duration?): Unit =
        withContext(Dispatchers.IO) {
            try {
                val bytes = objectMapper.writeValueAsBytes(value)
                if (ttl != null)
                    redis.opsForValue().set(key.value, bytes, ttl)
                else
                    redis.opsForValue().set(key.value, bytes)
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis put failed", ex)
            }
        }

    override suspend fun remove(key: CacheKey): Unit =
        withContext(Dispatchers.IO) {
            try { redis.delete(key.value) }
            catch (ex: RedisConnectionFailureException) { throw PersistenceException("Redis remove failed", ex) }
        }

    override suspend fun clear(): Unit =
        withContext(Dispatchers.IO) {
            try { redis.execute { it.serverCommands().flushDb() } }
            catch (ex: RedisConnectionFailureException) { throw PersistenceException("Redis clear failed", ex) }
        }
}
```

**Serialization:** `RedisTemplate<String, ByteArray>` stores raw bytes. `ObjectMapper` is injected — the consumer configures `KotlinModule` and `JavaTimeModule`. This avoids coupling the adapter to a specific Jackson configuration.

**Type erasure constraint:** Because `CachePort.get<T>` is non-reified, the type `T` is erased at runtime. The `ObjectMapper` **must** be configured with Jackson default typing enabled — e.g. `objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL)`. This embeds the concrete class name in the stored JSON (`{"@class": "com.example.MyClass", ...}`), allowing `readValue(bytes, Any::class.java)` to reconstruct the correct type. The cast to `T` is then safe. Consumers that use the reified `CachePort.get<T>` extension (from `commons-ports-cache`) work transparently — they just need the `ObjectMapper` to be configured correctly.

**No `@Component`:** Registration in the Spring context is the consumer's (or future Spring starter's) responsibility.

### Dependencies

```kotlin
api(project(":commons-ports-cache"))
api("org.springframework.boot:spring-boot-starter-data-redis")
api("com.fasterxml.jackson.core:jackson-databind")
api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
testImplementation(project(":commons-testkit-testcontainers"))
testImplementation(libs.testcontainers.generic)
```

### Testing

Redis Testcontainers singleton. `RedisTemplate` configured directly in tests (no full Spring context). Tests cover get/put/remove/clear and TTL expiry.

---

## Module: `commons-adapters-messaging-kafka`

**Port implemented:** `MessagePublisherPort`, `MessageConsumerPort` from `commons-ports-messaging`

**Convention plugin:** `kotlin-commons.gradle.kts` (no Spring)

### Publisher

```kotlin
class KafkaMessagePublisherAdapter(
    private val producer: KafkaProducer<String, ByteArray>,
) : MessagePublisherPort {
    override suspend fun publish(envelope: MessageEnvelope<ByteArray>): Unit =
        suspendCancellableCoroutine { cont ->
            val record = ProducerRecord(
                envelope.topic.value,
                envelope.headers.messageId.value,
                envelope.body,
            )
            producer.send(record) { _, ex ->
                if (ex != null) cont.resumeWithException(ex) else cont.resume(Unit)
            }
        }

    override suspend fun publishBatch(envelopes: List<MessageEnvelope<ByteArray>>): Unit =
        coroutineScope { envelopes.forEach { launch { publish(it) } } }
}
```

### Consumer

```kotlin
class KafkaMessageConsumerAdapter(
    private val consumer: KafkaConsumer<String, ByteArray>,
) : MessageConsumerPort {
    private val pending = ConcurrentHashMap<String, TopicPartitionOffset>()

    override suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>? =
        withContext(Dispatchers.IO) {
            consumer.subscribe(listOf(topic.value))
            consumer.poll(java.time.Duration.ofMillis(500))
                .firstOrNull()
                ?.let { record ->
                    val id = MessageId(record.key() ?: UUID.randomUUID().toString())
                    pending[id.value] = TopicPartitionOffset(record.topic(), record.partition(), record.offset())
                    record.toEnvelope(id, topic)
                }
        }

    override suspend fun acknowledge(messageId: MessageId): Unit =
        withContext(Dispatchers.IO) {
            pending.remove(messageId.value)?.let { tpo ->
                consumer.commitSync(tpo.toOffsetMap())
            }
        }

    override suspend fun nack(messageId: MessageId) {
        pending.remove(messageId.value) // offset not committed — Kafka redelivers on next session
    }
}
```

**`TopicPartitionOffset`** — internal data class holding `topic: String`, `partition: Int`, `offset: Long` with a `toOffsetMap()` method producing the `Map<TopicPartition, OffsetAndMetadata>` required by `KafkaConsumer.commitSync`.

**`group` parameter constraint:** Kafka's `group.id` is a `KafkaConsumer` configuration property — it cannot be changed per-call. The `group: ConsumerGroup` parameter in `receive()` must match the consumer's configured `group.id`; the adapter validates this with `require(consumer.groupMetadata().groupId() == group.value)` and throws `IllegalArgumentException` if they differ. The `KafkaConsumer` passed to the adapter must be pre-configured with the correct `group.id`.

**Thread-safety:** `KafkaConsumer` is not thread-safe. A single `KafkaMessageConsumerAdapter` instance must not be called concurrently from multiple coroutines. Consumer pooling is the consumer's responsibility.

**Semantics:** At-least-once delivery. `nack` discards the pending offset; next consumer session redelivers the message.

**`publishBatch`:** Uses `coroutineScope { launch {} }` — parallel sends, all must complete before returning.

### Dependencies

```kotlin
api(project(":commons-ports-messaging"))
api("org.apache.kafka:kafka-clients")
api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
testImplementation(project(":commons-testkit-testcontainers"))
testImplementation(libs.testcontainers.kafka)
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
```

### Testing

KafkaContainer Testcontainers singleton. Producer and consumer configured with `bootstrap.servers` from the container. Tests cover publish, receive, acknowledge, and nack (verify redelivery).

---

## Module: `commons-adapters-http-okhttp`

**Port implemented:** `HttpClientPort` from `commons-ports-http`

**Convention plugin:** `kotlin-commons.gradle.kts` (no Spring)

### Core class

```kotlin
class OkHttpClientAdapter(
    private val client: OkHttpClient,
) : HttpClientPort {
    override suspend fun execute(request: HttpRequest): HttpResponse<ByteArray> =
        suspendCancellableCoroutine { cont ->
            val call = client.newCall(request.toOkHttpRequest())
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) =
                    cont.resumeWithException(e)
                override fun onResponse(call: Call, response: Response) =
                    cont.resume(response.toHttpResponse())
            })
        }

    override suspend fun <T> execute(
        request: HttpRequest,
        mapper: (ByteArray) -> T,
    ): HttpResponse<T> = execute(request).map(mapper)
}
```

### Internal converters (not public)

- `HttpRequest.toOkHttpRequest(): Request` — builds OkHttp `Request` from URI, method, headers, body
- `HttpBody` → `RequestBody`:
  - `Bytes` → `content.toRequestBody(contentType.toMediaType())`
  - `FormUrlEncoded` → `FormBody.Builder`
  - `Multipart` → `MultipartBody.Builder`
  - `null` → `null` (GET/DELETE with no body)
- `Response.toHttpResponse(): HttpResponse<ByteArray>` — reads `body?.bytes()` and closes response
- `HttpResponse<ByteArray>.map(mapper: (ByteArray) -> T): HttpResponse<T>` — applies mapper to non-null body

**Cancellation:** `cont.invokeOnCancellation { call.cancel() }` propagates coroutine cancellation to the in-flight HTTP request.

**No retry/timeout defaults:** `OkHttpClient` is injected; the consumer configures timeouts, interceptors, and TLS.

### Dependencies

```kotlin
api(project(":commons-ports-http"))
api("com.squareup.okhttp3:okhttp")
api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
testImplementation("com.squareup.okhttp3:mockwebserver")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
```

### Testing

MockWebServer (OkHttp's built-in test server) — no Testcontainers, no real network. Tests cover all HTTP methods, all `HttpBody` variants, response mapping, and coroutine cancellation.

---

## Module: `commons-adapters-email-smtp`

**Port implemented:** `EmailPort` from `commons-ports-email`

**Convention plugin:** `kotlin-commons.gradle.kts` (no Spring)

### Core class

```kotlin
class SmtpEmailAdapter(
    private val session: Session,
) : EmailPort {
    override suspend fun send(email: Email): Unit =
        withContext(Dispatchers.IO) {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(email.from.value))
            email.to.forEach { message.addRecipient(Message.RecipientType.TO, InternetAddress(it.value)) }
            email.cc.forEach { message.addRecipient(Message.RecipientType.CC, InternetAddress(it.value)) }
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
}
```

### Internal extensions (not public)

- `Part.applyContent(content: EmailContent)` — sets `text/html` or `text/plain` based on `EmailContent` type
- `EmailAttachment.toMimeBodyPart(): MimeBodyPart` — wraps `ByteArray` in `DataHandler` with `ByteArrayDataSource`

**`jakarta.mail.Session` injected:** Consumer configures SMTP host, port, credentials, TLS via `Properties`. Supports plain SMTP, STARTTLS, relay-without-auth without adapter changes.

### Dependencies

```kotlin
api(project(":commons-ports-email"))
api("com.sun.mail:jakarta.mail")
api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
testImplementation(project(":commons-testkit-testcontainers"))
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
```

### Testing

GreenMail via `GreenMailContainers.instance`. Tests configure `Session` pointing at GreenMail's SMTP port, send email via the adapter, and assert via `greenMail.getReceivedMessages()`. Covers plain text, HTML, and attachments.

---

## Exception Handling Strategy

| Adapter | Library exception | Mapped to |
|---|---|---|
| JPA | `DataAccessException` | `PersistenceException` (port-defined) |
| Redis | `RedisConnectionFailureException` | `PersistenceException` (reused) |
| Kafka | `KafkaException` | Propagates as-is |
| OkHttp | `IOException` | Propagates as-is |
| SMTP | `MessagingException` | Propagates as-is |

Kafka, OkHttp, and SMTP do not define new exception types — resilience (retry, circuit breaker) is `commons-app-*` territory (Fase 3).

---

## BOM Update

`commons-bom/build.gradle.kts` adds 6 new constraints:

```kotlin
api(project(":commons-testkit-testcontainers"))
api(project(":commons-adapters-persistence-jpa"))
api(project(":commons-adapters-cache-redis"))
api(project(":commons-adapters-messaging-kafka"))
api(project(":commons-adapters-http-okhttp"))
api(project(":commons-adapters-email-smtp"))
```

---

## Out of Scope (Fase 2)

- Spring Boot auto-configuration (`@AutoConfiguration`, `spring.factories`) — Fase 3
- `commons-app-*` (retry, circuit breaker, idempotency, outbox, saga) — Fase 3
- Secondary ports (SMS, PDF, Excel) — future phases
- Interop with java-commons — future phases
- ArchUnit layer validation (`commons-testkit-archunit`) — Fase 3
