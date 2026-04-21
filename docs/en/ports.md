# Ports

Port modules define the interface contracts that application code depends on. They contain no implementation — only interfaces and value types. This is the hexagonal architecture pattern: the application core depends on port interfaces, and adapter modules provide the implementations.

Ports are small, focused modules. Add only a port dependency when you need the interface; add the corresponding adapter dependency when you need the implementation.

---

## ports-cache

**The interface contract:**

```kotlin
import com.marcusprado02.commons.ports.cache.*
import java.time.Duration

interface CachePort {
    suspend fun <T : Any> get(key: CacheKey, type: Class<T>): T?
    suspend fun <T : Any> put(key: CacheKey, value: T, ttl: Duration? = null)
    suspend fun remove(key: CacheKey)
    suspend fun clear()
    suspend fun exists(key: CacheKey): Boolean
}

// Reified helper — infers type from type parameter
suspend inline fun <reified T : Any> CachePort.get(key: CacheKey): T?

// Get or compute and store
suspend inline fun <reified T : Any> CachePort.getOrPut(
    key: CacheKey,
    ttl: Duration? = null,
    crossinline loader: suspend () -> T,
): T
```

**Consumer-side usage:**

```kotlin
class ProductService(
    private val cache: CachePort,
    private val productRepository: Repository<Product, ProductId>,
) {
    suspend fun findProduct(id: ProductId): Product? =
        cache.getOrPut(CacheKey("product:${id.value}"), ttl = Duration.ofMinutes(10)) {
            productRepository.findById(id)
        }

    suspend fun invalidateProduct(id: ProductId) {
        cache.remove(CacheKey("product:${id.value}"))
    }
}
```

**When to use vs. alternatives:** Use `CachePort` when you want your application logic testable without a real cache. If you need fine-grained Redis commands (ZADD, HSET, scripting), use a Redis client directly — the port abstracts the common read/write/ttl pattern only.

**Design decision:** The port interface uses `Class<T>` (Java class) rather than `KClass<T>` so that adapter implementations using Java-based serialisation (Jackson, Kryo) can work without extra wrapping. The reified inline extension `get<T>(key)` restores the ergonomic API for Kotlin callers.

---

## ports-persistence

**The interface contract:**

```kotlin
import com.marcusprado02.commons.ports.persistence.*

interface Repository<E : Any, I : Any> {
    suspend fun findById(id: I): E?
    suspend fun save(entity: E): E
    suspend fun saveAll(entities: Collection<E>): List<E>
    suspend fun delete(entity: E)
    suspend fun deleteById(id: I)
    suspend fun existsById(id: I): Boolean
}

interface PageableRepository<E : Any, I : Any> : Repository<E, I> {
    suspend fun findAll(request: PageRequest): PageResult<E>
    suspend fun count(): Long
}

data class PageRequest(
    val page: Int = 0,       // zero-based
    val size: Int = 20,
    val sort: List<SortField> = emptyList(),
)
data class SortField(val field: String, val direction: SortDirection = SortDirection.ASC)
enum class SortDirection { ASC, DESC }

data class PageResult<E>(
    val content: List<E>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    val totalPages: Int      // computed
    val isFirst: Boolean
    val isLast: Boolean
    val isEmpty: Boolean
}
```

**Consumer-side usage:**

```kotlin
class UserService(private val repo: PageableRepository<User, UserId>) {
    suspend fun listUsers(page: Int, size: Int): PageResult<User> =
        repo.findAll(
            PageRequest(
                page = page,
                size = size,
                sort = listOf(SortField("email"))
            )
        )

    suspend fun createUser(user: User): User = repo.save(user)

    suspend fun deleteUser(user: User) = repo.delete(user)
}
```

**When to use vs. alternatives:** Use these interfaces to keep domain services independent of JPA, MongoDB, or any specific persistence technology. If you need complex joins or native queries specific to one technology, implement a dedicated repository interface for that use case — do not add technology-specific methods to `Repository`.

**Design decision:** `Repository` and `PageableRepository` use separate interfaces rather than a single one with optional methods. This follows the Interface Segregation Principle: services that do not need pagination do not depend on pagination logic. `delete(entity: E)` takes the entity (not just the ID) because some persistence technologies use the whole entity for optimistic locking (version field comparison).

---

## ports-messaging

**The interface contract:**

```kotlin
import com.marcusprado02.commons.ports.messaging.*

interface MessagePublisherPort {
    suspend fun publish(envelope: MessageEnvelope<*>)
    suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>)
}

interface MessageConsumerPort {
    suspend fun receive(topic: TopicName, group: ConsumerGroup): MessageEnvelope<ByteArray>?
    suspend fun acknowledge(messageId: MessageId)
    suspend fun nack(messageId: MessageId)
    suspend fun poll(topic: TopicName, group: ConsumerGroup, maxCount: Int): List<MessageEnvelope<ByteArray>>
}

interface DeadLetterPort {
    suspend fun send(envelope: MessageEnvelope<ByteArray>, reason: String, originalTopic: TopicName)
}

data class MessageEnvelope<T>(val topic: TopicName, val body: T, val headers: MessageHeaders)
```

**Consumer-side usage:**

```kotlin
class OrderEventPublisher(private val publisher: MessagePublisherPort) {
    suspend fun publishOrderCreated(event: OrderCreatedEvent) {
        val envelope = MessageEnvelope(
            topic   = TopicName("orders.created"),
            body    = Json.encodeToString(event).toByteArray(),
            headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now())
        )
        publisher.publish(envelope)
    }
}
```

**When to use vs. alternatives:** Use `MessagePublisherPort` and `MessageConsumerPort` when your application logic should be testable without a running broker. If you need Kafka-specific features (transactions, compaction, stream processing), use the Kafka client directly alongside the port for simple publish/consume.

**Design decision:** `MessageEnvelope` is generic over `T` because different adapters support different body types (`ByteArray` for Kafka). The `DeadLetterPort` is a separate interface so dead-letter handling can be swapped independently (e.g., log to database vs. publish to a DLQ topic). The interface takes the full `envelope` (not just an ID) so the dead-letter handler has the original message payload available.

---

## ports-http

**The interface contract:**

```kotlin
import com.marcusprado02.commons.ports.http.*
import java.net.URI

interface HttpClientPort {
    suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>
    suspend fun <T> execute(request: HttpRequest, mapper: (ByteArray) -> T): HttpResponse<T>
}

// Convenience extensions
suspend fun HttpClientPort.get(uri: URI): HttpResponse<ByteArray>
suspend fun HttpClientPort.post(uri: URI, body: HttpBody): HttpResponse<ByteArray>
suspend fun HttpClientPort.put(uri: URI, body: HttpBody): HttpResponse<ByteArray>
suspend fun HttpClientPort.delete(uri: URI): HttpResponse<ByteArray>

sealed class HttpBody {
    class Bytes(val content: ByteArray, val contentType: String) : HttpBody()
    data class FormUrlEncoded(val params: Map<String, String>) : HttpBody()
    data class Multipart(val parts: List<MultipartPart>) : HttpBody()
    class Json<T>(val value: T, val serializer: KSerializer<T>, val format: Json = Json) : HttpBody()
}

data class HttpResponse<T>(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: T?,
) {
    val isSuccessful: Boolean   // 2xx
    val isClientError: Boolean  // 4xx
    val isServerError: Boolean  // 5xx
}
```

**Consumer-side usage:**

```kotlin
class PaymentGatewayClient(private val http: HttpClientPort) {
    suspend fun charge(payload: ChargeRequest): ChargeResponse {
        val response = http.post(
            uri  = URI.create("https://payments.example.com/charge"),
            body = HttpBody.Json(payload, ChargeRequest.serializer())
        )
        check(response.isSuccessful) { "Charge failed: ${response.statusCode}" }
        return Json.decodeFromString(response.body!!.toString(Charsets.UTF_8))
    }

    suspend fun getStatus(id: String): HttpResponse<ByteArray> =
        http.get(URI.create("https://payments.example.com/charges/$id"))
}
```

**When to use vs. alternatives:** Use `HttpClientPort` when you need to mock outbound HTTP in tests (e.g., with WireMock). If you need streaming responses, WebSocket, or HTTP/2 push, use the OkHttp client directly — the port covers the standard request/response pattern only.

**Design decision:** The core method is `execute(HttpRequest)` rather than separate `get`/`post`/`put`/`delete` methods. This keeps the interface minimal and avoids combinatorial explosion when adding headers, timeouts, or retry policies. Convenience extensions for the common verbs are defined at the port level so they work with any implementation.

---

## ports-email

**The interface contract:**

```kotlin
import com.marcusprado02.commons.ports.email.*

interface EmailPort {
    suspend fun send(email: Email)
    suspend fun sendBatch(emails: List<Email>)
    suspend fun sendTemplate(templateId: String, context: Map<String, Any>, to: EmailAddress)
}

data class Email(
    val from: EmailAddress,
    val to: List<EmailAddress>,       // must not be empty
    val subject: String,               // must not be blank
    val content: EmailContent,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val replyTo: EmailAddress? = null,
    val attachments: List<EmailAttachment> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
)

data class EmailContent(
    val html: String? = null,  // at least one of html/plain must be non-null
    val plain: String? = null,
)

data class EmailAddress(
    val address: String,           // must contain @
    val displayName: String? = null,
)
```

**Consumer-side usage:**

```kotlin
class WelcomeEmailService(private val email: EmailPort) {
    suspend fun sendWelcome(user: User) {
        email.send(
            Email(
                from    = EmailAddress("noreply@example.com", "Example Platform"),
                to      = listOf(EmailAddress(user.email, user.name)),
                subject = "Welcome to the platform",
                content = EmailContent(
                    plain = "Welcome, ${user.name}! Visit https://example.com to get started.",
                    html  = "<h1>Welcome, ${user.name}!</h1><p>Visit <a href='https://example.com'>example.com</a>.</p>"
                )
            )
        )
    }
}
```

**When to use vs. alternatives:** Use `EmailPort` when you want to test email-sending logic without an SMTP server (replace the port with a no-op in tests). If you need advanced features (templating engines, email tracking, bulk sending), use an email service SDK directly and implement the port as a thin adapter.

**Design decision:** `Email` and `EmailContent` are separate types rather than a flat data class. This separation allows `EmailContent` to carry both `html` and `plain` variants independently, supporting multi-part MIME emails without the API growing fields for every combination. `EmailAddress` validates the `@` symbol at construction rather than at send time so errors surface early.
