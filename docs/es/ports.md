# Ports

Los módulos de port definen los contratos de interfaz de los que depende el código de aplicación. No contienen implementación — solo interfaces y tipos de valor. Este es el patrón de arquitectura hexagonal: el núcleo de la aplicación depende de las interfaces de port, y los módulos de adapter proveen las implementaciones.

Los ports son módulos pequeños y enfocados. Agrega solo la dependencia de un port cuando necesites la interfaz; agrega la dependencia del adapter correspondiente cuando necesites la implementación.

---

## ports-cache

**El contrato de interfaz:**

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

// Helper reificado — infiere el tipo desde el parámetro de tipo
suspend inline fun <reified T : Any> CachePort.get(key: CacheKey): T?

// Obtener o calcular y almacenar
suspend inline fun <reified T : Any> CachePort.getOrPut(
    key: CacheKey,
    ttl: Duration? = null,
    crossinline loader: suspend () -> T,
): T
```

**Uso desde el lado consumidor:**

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

**Cuándo usarlo frente a alternativas:** Usa `CachePort` cuando quieras que tu lógica de aplicación sea comprobable sin una caché real. Si necesitas comandos Redis detallados (ZADD, HSET, scripting), usa un cliente Redis directamente — el port abstrae solo el patrón común de lectura/escritura/ttl.

**Decisión de diseño:** La interfaz del port usa `Class<T>` (clase Java) en lugar de `KClass<T>` para que las implementaciones de adapter que usan serialización basada en Java (Jackson, Kryo) puedan funcionar sin envoltura adicional. La extensión inline reificada `get<T>(key)` restaura la API ergonómica para los llamadores de Kotlin.

---

## ports-persistence

**El contrato de interfaz:**

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
    val page: Int = 0,       // base cero
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
    val totalPages: Int      // calculado
    val isFirst: Boolean
    val isLast: Boolean
    val isEmpty: Boolean
}
```

**Uso desde el lado consumidor:**

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

**Cuándo usarlo frente a alternativas:** Usa estas interfaces para mantener los servicios de dominio independientes de JPA, MongoDB o cualquier tecnología de persistencia específica. Si necesitas joins complejos o consultas nativas específicas de una tecnología, implementa una interfaz de repositorio dedicada para ese caso — no agregues métodos específicos de tecnología a `Repository`.

**Decisión de diseño:** `Repository` y `PageableRepository` son interfaces separadas en lugar de una sola con métodos opcionales. Esto sigue el Principio de Segregación de Interfaces: los servicios que no necesitan paginación no dependen de la lógica de paginación. `delete(entity: E)` recibe la entidad (no solo el ID) porque algunas tecnologías de persistencia usan la entidad completa para el bloqueo optimista (comparación del campo version).

---

## ports-messaging

**El contrato de interfaz:**

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

**Uso desde el lado consumidor:**

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

**Cuándo usarlo frente a alternativas:** Usa `MessagePublisherPort` y `MessageConsumerPort` cuando tu lógica de aplicación deba ser comprobable sin un broker en ejecución. Si necesitas características específicas de Kafka (transacciones, compactación, procesamiento de streams), usa el cliente Kafka directamente junto con el port para publicar/consumir simple.

**Decisión de diseño:** `MessageEnvelope` es genérico sobre `T` porque diferentes adapters soportan diferentes tipos de cuerpo (`ByteArray` para Kafka). `DeadLetterPort` es una interfaz separada para que el manejo de dead-letter pueda intercambiarse de forma independiente (por ejemplo, registrar en base de datos vs. publicar en un tópico DLQ). La interfaz recibe el `envelope` completo (no solo un ID) para que el manejador de dead-letter tenga disponible el payload del mensaje original.

---

## ports-http

**El contrato de interfaz:**

```kotlin
import com.marcusprado02.commons.ports.http.*
import java.net.URI

interface HttpClientPort {
    suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>
    suspend fun <T> execute(request: HttpRequest, mapper: (ByteArray) -> T): HttpResponse<T>
}

// Extensiones de conveniencia
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

**Uso desde el lado consumidor:**

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

**Cuándo usarlo frente a alternativas:** Usa `HttpClientPort` cuando necesites simular HTTP saliente en pruebas (por ejemplo, con WireMock). Si necesitas respuestas en streaming, WebSocket o HTTP/2 push, usa el cliente OkHttp directamente — el port cubre solo el patrón estándar de solicitud/respuesta.

**Decisión de diseño:** El método principal es `execute(HttpRequest)` en lugar de métodos separados `get`/`post`/`put`/`delete`. Esto mantiene la interfaz mínima y evita la explosión combinatoria al agregar cabeceras, timeouts o políticas de reintentos. Las extensiones de conveniencia para los verbos comunes se definen a nivel del port para que funcionen con cualquier implementación.

---

## ports-email

**El contrato de interfaz:**

```kotlin
import com.marcusprado02.commons.ports.email.*

interface EmailPort {
    suspend fun send(email: Email)
    suspend fun sendBatch(emails: List<Email>)
    suspend fun sendTemplate(templateId: String, context: Map<String, Any>, to: EmailAddress)
}

data class Email(
    val from: EmailAddress,
    val to: List<EmailAddress>,       // no debe estar vacío
    val subject: String,               // no debe estar en blanco
    val content: EmailContent,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val replyTo: EmailAddress? = null,
    val attachments: List<EmailAttachment> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
)

data class EmailContent(
    val html: String? = null,  // al menos uno de html/plain debe ser no nulo
    val plain: String? = null,
)

data class EmailAddress(
    val address: String,           // debe contener @
    val displayName: String? = null,
)
```

**Uso desde el lado consumidor:**

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

**Cuándo usarlo frente a alternativas:** Usa `EmailPort` cuando quieras probar la lógica de envío de correos sin un servidor SMTP (reemplaza el port con un no-op en las pruebas). Si necesitas funciones avanzadas (motores de plantillas, seguimiento de correos, envío masivo), usa un SDK de servicio de correo directamente e implementa el port como un adapter delgado.

**Decisión de diseño:** `Email` y `EmailContent` son tipos separados en lugar de una sola data class plana. Esta separación permite que `EmailContent` lleve las variantes `html` y `plain` de forma independiente, soportando correos MIME multiparte sin que la API crezca con campos para cada combinación. `EmailAddress` valida el símbolo `@` en la construcción en lugar de al momento del envío, para que los errores aparezcan temprano.
