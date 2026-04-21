# Adapters

Adapter modules implement port interfaces using specific libraries. Each adapter depends on its port and provides the wiring needed to connect an infrastructure technology to your application.

Add an adapter dependency only when you need the concrete implementation. The port dependency alone is sufficient for application code and unit tests.

---

## adapters-cache-redis

**Implements:** `CachePort` using Spring Data Redis (`RedisTemplate<String, ByteArray>`) with Jackson serialisation.

**Dependency:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-cache-redis")
```

**Wiring:**

```kotlin
@Configuration
class CacheConfig(private val redis: RedisTemplate<String, ByteArray>) {
    @Bean
    fun cachePort(): CachePort = RedisCacheAdapter(redis)

    // Custom ObjectMapper (optional)
    @Bean
    fun cachePortCustom(redis: RedisTemplate<String, ByteArray>, mapper: ObjectMapper): CachePort =
        RedisCacheAdapter(redis, objectMapper = mapper)
}
```

**Complete usage example:**

```kotlin
// Application code — depends on port, not adapter
class SessionService(private val cache: CachePort) {
    suspend fun getSession(token: String): Session? =
        cache.get<Session>(CacheKey("session:$token"))

    suspend fun storeSession(token: String, session: Session) =
        cache.put(CacheKey("session:$token"), session, ttl = Duration.ofMinutes(30))

    suspend fun invalidateSession(token: String) =
        cache.remove(CacheKey("session:$token"))
}
```

**When to consider an alternative:** Use `RedisClusterCacheAdapter` (also in this module) for Redis Cluster topology. Use a different adapter entirely if you need Redis scripting (`EVAL`), sorted sets, pub/sub, or cluster-specific features — `RedisCacheAdapter` implements only the `CachePort` get/put/remove/clear/exists contract.

---

## adapters-persistence-jpa

**Implements:** `Repository<E, I>` (via `JpaRepositoryAdapter`) and `PageableRepository<E, I>` (via `JpaPageableRepositoryAdapter`) using Spring Data JPA. Both are abstract classes — subclass them for each aggregate.

**Dependency:**

```kotlin
implementation("io.github.marcusprado02.commons:commons-adapters-persistence-jpa")
```

**Wiring:**

```kotlin
// Extend JpaPageableRepositoryAdapter for aggregates that need pagination
@Repository
class UserJpaAdapter(jpa: JpaRepository<UserEntity, String>) :
    JpaPageableRepositoryAdapter<UserEntity, String>(jpa)

// Extend JpaRepositoryAdapter for aggregates that don't need pagination
@Repository
class AuditLogJpaAdapter(jpa: JpaRepository<AuditLogEntity, Long>) :
    JpaRepositoryAdapter<AuditLogEntity, Long>(jpa)
```

**Complete usage example:**

```kotlin
// Domain service — depends on PageableRepository<User, UserId>
class UserService(private val repo: PageableRepository<UserEntity, String>) {
    suspend fun createUser(entity: UserEntity): UserEntity = repo.save(entity)

    suspend fun getPage(page: Int, size: Int): PageResult<UserEntity> =
        repo.findAll(
            PageRequest(
                page = page,
                size = size,
                sort = listOf(SortField("email"))
            )
        )

    suspend fun deleteUser(entity: UserEntity) = repo.delete(entity)
}
```

**When to consider an alternative:** If you need JOOQ, native queries, multi-tenancy schema switching, or a non-JPA ORM (Exposed, MyBatis), implement the `Repository` interface directly against that technology.

---

## adapters-messaging-kafka

**Implements:** `MessagePublisherPort` (`KafkaMessagePublisherAdapter`) and `MessageConsumerPort` (`KafkaMessageConsumerAdapter`) using the Apache Kafka Java client.

**Dependency:**

```kotlin
implementation("io.github.marcusprado02.commons:commons-adapters-messaging-kafka")
```

**Wiring:**

```kotlin
@Configuration
class KafkaConfig {
    @Bean
    fun kafkaPublisher(): MessagePublisherPort {
        val producer = KafkaProducer<String, ByteArray>(mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java.name,
        ))
        return KafkaMessagePublisherAdapter(producer)
    }

    @Bean
    fun kafkaConsumer(deadLetter: DeadLetterPort): MessageConsumerPort {
        val consumer = KafkaConsumer<String, ByteArray>(mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "my-service-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ))
        return KafkaMessageConsumerAdapter(
            consumer       = consumer,
            groupId        = "my-service-group",
            maxNacks       = 3,
            deadLetterPort = deadLetter,
            retryPolicy    = RetryPolicy(initialDelayMs = 100, multiplier = 2.0, maxDelayMs = 10_000),
        )
    }
}
```

**Dead-letter port:** When a message is nacked `maxNacks` times, `KafkaMessageConsumerAdapter` calls `DeadLetterPort.send(envelope, reason, originalTopic)`. Implement `DeadLetterPort` to publish to a DLQ topic or log to a database.

**Complete usage example:**

```kotlin
val envelope = MessageEnvelope(
    topic   = TopicName("orders.created"),
    body    = Json.encodeToString(event).toByteArray(),
    headers = MessageHeaders(
        messageId     = MessageId.generate(),
        timestamp     = Instant.now(),
        correlationId = requestId,
    )
)
publisher.publish(envelope)

// Consumer side
val received = consumer.receive(TopicName("orders.created"), ConsumerGroup("my-group"))
if (received != null) {
    processOrder(received)
    consumer.acknowledge(received.headers.messageId)
}
```

**When to consider an alternative:** Use Kafka Streams or a Spring Kafka consumer directly if you need stream processing, windowed aggregations, or exactly-once semantics at the consumer level.

---

## adapters-http-okhttp

**Implements:** `HttpClientPort` (`OkHttpClientAdapter`) using OkHttp. Use `OkHttpClientBuilder` to configure the underlying `OkHttpClient`, then wrap it.

**Dependency:**

```kotlin
implementation("io.github.marcusprado02.commons:commons-adapters-http-okhttp")
```

**Wiring:**

```kotlin
@Bean
fun httpClientPort(): HttpClientPort {
    val okhttp = OkHttpClientBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(30))
        .writeTimeout(Duration.ofSeconds(30))
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(RetryInterceptor(maxRetries = 3))
        .build()
    return OkHttpClientAdapter(okhttp)
}
```

**Complete usage example:**

```kotlin
class ExternalApiClient(private val http: HttpClientPort) {
    suspend fun fetchData(id: String): DataResponse {
        val response = http.get(URI.create("https://api.example.com/data/$id"))
        check(response.isSuccessful) { "API returned ${response.statusCode}" }
        return Json.decodeFromString(response.body!!.toString(Charsets.UTF_8))
    }

    suspend fun postData(payload: DataPayload): HttpResponse<ByteArray> =
        http.post(
            uri  = URI.create("https://api.example.com/data"),
            body = HttpBody.Json(payload, DataPayload.serializer())
        )
}
```

Available interceptors in this module:
- `LoggingInterceptor()` — logs requests and responses
- `RetryInterceptor(maxRetries, delayMillis)` — retries on network failure
- `CircuitBreakerInterceptor(circuitBreaker)` — wraps a Resilience4j `CircuitBreaker`

**When to consider an alternative:** Use OkHttp directly if you need streaming responses, WebSockets, HTTP/2 server push, or cookie management. The adapter provides the standard `HttpClientPort` contract only.

---

## adapters-email-smtp

**Implements:** `EmailPort` (`SmtpEmailAdapter`) using Jakarta Mail over SMTP. Use `SmtpSessionBuilder` to configure the Jakarta Mail `Session`.

**Dependency:**

```kotlin
implementation("io.github.marcusprado02.commons:commons-adapters-email-smtp")
```

**Wiring:**

```kotlin
@Bean
fun emailPort(): EmailPort {
    val session = SmtpSessionBuilder()
        .host("smtp.example.com")
        .port(587)
        .credentials("noreply@example.com", System.getenv("SMTP_PASSWORD"))
        .tls(true)
        .connectTimeout(5_000)
        .readTimeout(10_000)
        .build()
    return SmtpEmailAdapter(session)
}
```

**Complete usage example:**

```kotlin
class NotificationService(private val email: EmailPort) {
    suspend fun sendPasswordReset(user: User, resetLink: String) {
        email.send(
            Email(
                from    = EmailAddress("noreply@example.com", "Example Platform"),
                to      = listOf(EmailAddress(user.email, user.name)),
                subject = "Password Reset Request",
                content = EmailContent(
                    plain = "Reset your password: $resetLink",
                    html  = "<p>Reset your password: <a href='$resetLink'>click here</a></p>"
                )
            )
        )
    }
}
```

**When to consider an alternative:** Use SendGrid, AWS SES, or Mailgun SDKs directly if you need delivery tracking, bounce handling, bulk sending, or template management. Implement the `EmailPort` interface as a thin wrapper over those SDKs to keep your application code decoupled.

---

## testkit-testcontainers

**What it provides:** Pre-configured singleton Testcontainers instances for integration tests. Containers start once per Gradle test JVM and are shared across all tests in the run.

**Dependency:**

```kotlin
testImplementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
testImplementation("io.github.marcusprado02.commons:commons-testkit-testcontainers")
```

**Available singletons:**

| Object | Property | Type |
|---|---|---|
| `KafkaContainers` | `instance` | `KafkaContainer` |
| `KafkaContainers` | `schemaRegistry` | `GenericContainer<*>` |
| `KafkaContainers` | `schemaRegistryUrl` | `String` |
| `PostgresContainers` | `instance` | `PostgreSQLContainer<*>` |
| `MongoContainers` | `instance` | `MongoDBContainer` |
| `MySqlContainers` | `instance` | `MySQLContainer<*>` |
| `RedisContainers` | `instance` | `GenericContainer<*>` |
| `WireMockContainers` | `instance` | `WireMockContainer` |
| `GreenMailContainers` | `instance` | `GenericContainer<*>` |
| `LocalStackContainers` | `instance` | `LocalStackContainer` |

**Complete usage example:**

```kotlin
import com.marcusprado02.commons.testkit.testcontainers.*
import io.kotest.core.spec.style.FunSpec

class KafkaIntegrationTest : FunSpec({
    // Singleton — container starts once for the entire test run
    val bootstrap = KafkaContainers.instance.bootstrapServers

    test("can publish and receive a message") {
        val producer = KafkaProducer<String, ByteArray>(mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java.name,
        ))
        // test logic
        producer.close()
    }
})

class RedisIntegrationTest : FunSpec({
    val redisPort = RedisContainers.instance.getMappedPort(6379)
    val redisHost = RedisContainers.instance.host

    test("can store and retrieve a value") {
        // wire RedisCacheAdapter using redisHost:redisPort
    }
})
```

**When to consider an alternative:** Use `@Testcontainers` + `@Container` (JUnit 5 extension) if you need per-test container lifecycle, parallel test execution with isolated containers, or containers not covered by this testkit.
