# Adapters

Los módulos de adapter implementan las interfaces de port utilizando bibliotecas específicas. Cada adapter depende de su port y provee el cableado necesario para conectar una tecnología de infraestructura a tu aplicación.

Agrega una dependencia de adapter solo cuando necesites la implementación concreta. La dependencia del port por sí sola es suficiente para el código de aplicación y las pruebas unitarias.

---

## adapters-cache-redis

**Implementa:** `CachePort` usando Spring Data Redis (`RedisTemplate<String, ByteArray>`) con serialización Jackson.

**Dependencia:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-cache-redis")
```

**Configuración:**

```kotlin
@Configuration
class CacheConfig(private val redis: RedisTemplate<String, ByteArray>) {
    @Bean
    fun cachePort(): CachePort = RedisCacheAdapter(redis)

    // ObjectMapper personalizado (opcional)
    @Bean
    fun cachePortCustom(redis: RedisTemplate<String, ByteArray>, mapper: ObjectMapper): CachePort =
        RedisCacheAdapter(redis, objectMapper = mapper)
}
```

**Ejemplo de uso completo:**

```kotlin
// Código de aplicación — depende del port, no del adapter
class SessionService(private val cache: CachePort) {
    suspend fun getSession(token: String): Session? =
        cache.get<Session>(CacheKey("session:$token"))

    suspend fun storeSession(token: String, session: Session) =
        cache.put(CacheKey("session:$token"), session, ttl = Duration.ofMinutes(30))

    suspend fun invalidateSession(token: String) =
        cache.remove(CacheKey("session:$token"))
}
```

**Cuándo considerar una alternativa:** Usa `RedisClusterCacheAdapter` (también en este módulo) para topología Redis Cluster. Usa un adapter diferente si necesitas scripting Redis (`EVAL`), sorted sets, pub/sub o características específicas de cluster — `RedisCacheAdapter` implementa solo el contrato `CachePort` de get/put/remove/clear/exists.

---

## adapters-persistence-jpa

**Implementa:** `Repository<E, I>` (vía `JpaRepositoryAdapter`) y `PageableRepository<E, I>` (vía `JpaPageableRepositoryAdapter`) usando Spring Data JPA. Ambas son clases abstractas — debes crear una subclase para cada agregado.

**Dependencia:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-persistence-jpa")
```

**Configuración:**

```kotlin
// Extiende JpaPageableRepositoryAdapter para agregados que necesitan paginación
@Repository
class UserJpaAdapter(jpa: JpaRepository<UserEntity, String>) :
    JpaPageableRepositoryAdapter<UserEntity, String>(jpa)

// Extiende JpaRepositoryAdapter para agregados que no necesitan paginación
@Repository
class AuditLogJpaAdapter(jpa: JpaRepository<AuditLogEntity, Long>) :
    JpaRepositoryAdapter<AuditLogEntity, Long>(jpa)
```

**Ejemplo de uso completo:**

```kotlin
// Servicio de dominio — depende de PageableRepository<User, UserId>
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

**Cuándo considerar una alternativa:** Si necesitas JOOQ, consultas nativas, cambio de esquema multi-tenant o un ORM que no sea JPA (Exposed, MyBatis), implementa la interfaz `Repository` directamente contra esa tecnología.

---

## adapters-messaging-kafka

**Implementa:** `MessagePublisherPort` (`KafkaMessagePublisherAdapter`) y `MessageConsumerPort` (`KafkaMessageConsumerAdapter`) usando el cliente Java de Apache Kafka.

**Dependencia:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-messaging-kafka")
```

**Configuración:**

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

**Port de dead-letter:** Cuando un mensaje recibe nack `maxNacks` veces, `KafkaMessageConsumerAdapter` llama a `DeadLetterPort.send(envelope, reason, originalTopic)`. Implementa `DeadLetterPort` para publicar en un tópico DLQ o registrar en una base de datos.

**Ejemplo de uso completo:**

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

// Lado consumidor
val received = consumer.receive(TopicName("orders.created"), ConsumerGroup("my-group"))
if (received != null) {
    processOrder(received)
    consumer.acknowledge(received.headers.messageId)
}
```

**Cuándo considerar una alternativa:** Usa Kafka Streams o un consumidor Spring Kafka directamente si necesitas procesamiento de streams, agregaciones por ventana o semántica exactly-once a nivel del consumidor.

---

## adapters-http-okhttp

**Implementa:** `HttpClientPort` (`OkHttpClientAdapter`) usando OkHttp. Usa `OkHttpClientBuilder` para configurar el `OkHttpClient` subyacente y luego envuélvelo.

**Dependencia:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-http-okhttp")
```

**Configuración:**

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

**Ejemplo de uso completo:**

```kotlin
class ExternalApiClient(private val http: HttpClientPort) {
    suspend fun fetchData(id: String): DataResponse {
        val response = http.get(URI.create("https://api.example.com/data/$id"))
        check(response.isSuccessful) { "API returned ${response.statusCode}" }
        val bytes = response.body ?: error("no response body")
        return Json.decodeFromString(String(bytes, Charsets.UTF_8))
    }

    suspend fun postData(payload: DataPayload): HttpResponse<ByteArray> =
        http.post(
            uri  = URI.create("https://api.example.com/data"),
            body = HttpBody.Json(payload, DataPayload.serializer())
        )
}
```

Interceptores disponibles en este módulo:
- `LoggingInterceptor()` — registra solicitudes y respuestas
- `RetryInterceptor(maxRetries, delayMillis)` — reintenta en fallos de red
- `CircuitBreakerInterceptor(circuitBreaker)` — envuelve un `CircuitBreaker` de Resilience4j

**Cuándo considerar una alternativa:** Usa OkHttp directamente si necesitas respuestas en streaming, WebSockets, HTTP/2 server push o manejo de cookies. El adapter provee solo el contrato estándar `HttpClientPort`.

---

## adapters-email-smtp

**Implementa:** `EmailPort` (`SmtpEmailAdapter`) usando Jakarta Mail sobre SMTP. Usa `SmtpSessionBuilder` para configurar la `Session` de Jakarta Mail.

**Dependencia:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-email-smtp")
```

**Configuración:**

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

**Ejemplo de uso completo:**

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

**Cuándo considerar una alternativa:** Usa SDKs de SendGrid, AWS SES o Mailgun directamente si necesitas seguimiento de entregas, manejo de rebotes, envío masivo o gestión de plantillas. Implementa la interfaz `EmailPort` como un wrapper delgado sobre esos SDKs para mantener el código de aplicación desacoplado.

---

## testkit-testcontainers

**Qué provee:** Instancias singleton preconfiguradas de Testcontainers para pruebas de integración. Los contenedores se inician una sola vez por JVM de prueba de Gradle y se comparten entre todas las pruebas de la ejecución.

**Dependencia:**

```kotlin
testImplementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
testImplementation("io.github.marcusprado02.commons:commons-testkit-testcontainers")
```

**Singletons disponibles:**

| Objeto | Propiedad | Tipo |
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

**Ejemplo de uso completo:**

```kotlin
import com.marcusprado02.commons.testkit.testcontainers.*
import io.kotest.core.spec.style.FunSpec

class KafkaIntegrationTest : FunSpec({
    // Singleton — el contenedor se inicia una sola vez en toda la ejecución
    val bootstrap = KafkaContainers.instance.bootstrapServers

    test("can publish and receive a message") {
        val producer = KafkaProducer<String, ByteArray>(mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrap,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java.name,
        ))
        // lógica de prueba
        producer.close()
    }
})

class RedisIntegrationTest : FunSpec({
    val redisPort = RedisContainers.instance.getMappedPort(6379)
    val redisHost = RedisContainers.instance.host

    test("can store and retrieve a value") {
        // configurar RedisCacheAdapter usando redisHost:redisPort
    }
})
```

**Cuándo considerar una alternativa:** Usa `@Testcontainers` + `@Container` (extensión JUnit 5) si necesitas ciclo de vida del contenedor por prueba, ejecución de pruebas en paralelo con contenedores aislados, o contenedores no cubiertos por este testkit.
