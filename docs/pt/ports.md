# Ports

Módulos de port definem os contratos de interface dos quais o código de aplicação depende. Não contêm implementação — apenas interfaces e tipos de valor. Este é o padrão de arquitetura hexagonal.

Ports são módulos pequenos e focados. Adicione apenas a dependência de port quando precisar da interface; adicione a dependência do adapter correspondente quando precisar da implementação.

---

## ports-cache

**Contrato da interface:**

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

// Helper reificado — infere o tipo pelo parâmetro de tipo
suspend inline fun <reified T : Any> CachePort.get(key: CacheKey): T?

// Obtém ou calcula e armazena
suspend inline fun <reified T : Any> CachePort.getOrPut(
    key: CacheKey,
    ttl: Duration? = null,
    crossinline loader: suspend () -> T,
): T
```

**Uso pelo consumidor:**

```kotlin
class ProdutoService(
    private val cache: CachePort,
    private val repositorioProduto: Repository<Produto, ProdutoId>,
) {
    suspend fun buscarProduto(id: ProdutoId): Produto? =
        cache.getOrPut(CacheKey("produto:${id.value}"), ttl = Duration.ofMinutes(10)) {
            repositorioProduto.findById(id)
        }

    suspend fun invalidarProduto(id: ProdutoId) {
        cache.remove(CacheKey("produto:${id.value}"))
    }
}
```

**Quando usar vs. alternativas:** Use `CachePort` quando quiser que a lógica de aplicação seja testável sem um cache real. Se precisar de comandos Redis específicos (ZADD, HSET, scripting), use o cliente Redis diretamente.

**Decisão de design:** A interface usa `Class<T>` (Java class) em vez de `KClass<T>` para que implementações baseadas em Java possam trabalhar sem wrapping extra. A extensão reificada `get<T>(key)` restaura a API ergonômica para chamadores Kotlin.

---

## ports-persistence

**Contrato da interface:**

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
    val page: Int = 0,       // índice base zero
    val size: Int = 20,
    val sort: List<SortField> = emptyList(),
)
data class SortField(val field: String, val direction: SortDirection = SortDirection.ASC)
enum class SortDirection { ASC, DESC }
```

**Uso pelo consumidor:**

```kotlin
class UsuarioService(private val repo: PageableRepository<UsuarioEntity, String>) {
    suspend fun listarUsuarios(pagina: Int, tamanho: Int): PageResult<UsuarioEntity> =
        repo.findAll(
            PageRequest(
                page = pagina,
                size = tamanho,
                sort = listOf(SortField("email"))
            )
        )

    suspend fun criarUsuario(entity: UsuarioEntity): UsuarioEntity = repo.save(entity)

    suspend fun deletarUsuario(entity: UsuarioEntity) = repo.delete(entity)
}
```

**Decisão de design:** `Repository` e `PageableRepository` são interfaces separadas (Princípio de Segregação de Interface). `delete(entity: E)` recebe a entidade (não apenas o ID) porque algumas tecnologias de persistência usam a entidade completa para bloqueio otimista.

---

## ports-messaging

**Contrato da interface:**

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

**Uso pelo consumidor:**

```kotlin
class PublicadorEventoPedido(private val publisher: MessagePublisherPort) {
    suspend fun publicarPedidoCriado(evento: PedidoCriadoEvento) {
        publisher.publish(MessageEnvelope(
            topic   = TopicName("pedidos.criados"),
            body    = Json.encodeToString(evento).toByteArray(),
            headers = MessageHeaders(messageId = MessageId.generate(), timestamp = Instant.now())
        ))
    }
}
```

**Decisão de design:** `DeadLetterPort` é uma interface separada para que o tratamento de dead-letter possa ser trocado independentemente. Recebe o `envelope` completo para que o handler tenha o payload original disponível.

---

## ports-http

**Contrato da interface:**

```kotlin
import com.marcusprado02.commons.ports.http.*
import java.net.URI

interface HttpClientPort {
    suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>
    suspend fun <T> execute(request: HttpRequest, mapper: (ByteArray) -> T): HttpResponse<T>
}

// Extensões de conveniência
suspend fun HttpClientPort.get(uri: URI): HttpResponse<ByteArray>
suspend fun HttpClientPort.post(uri: URI, body: HttpBody): HttpResponse<ByteArray>
suspend fun HttpClientPort.put(uri: URI, body: HttpBody): HttpResponse<ByteArray>
suspend fun HttpClientPort.delete(uri: URI): HttpResponse<ByteArray>

sealed class HttpBody {
    class Bytes(val content: ByteArray, val contentType: String) : HttpBody()
    data class FormUrlEncoded(val params: Map<String, String>) : HttpBody()
    data class Multipart(val parts: List<MultipartPart>) : HttpBody()
    class Json<T>(val value: T, val serializer: KSerializer<T>) : HttpBody()
}
```

**Decisão de design:** O método central é `execute(HttpRequest)` em vez de métodos separados por verbo. As extensões de conveniência para verbos comuns são definidas no módulo de port para que funcionem com qualquer implementação.

---

## ports-email

**Contrato da interface:**

```kotlin
import com.marcusprado02.commons.ports.email.*

interface EmailPort {
    suspend fun send(email: Email)
    suspend fun sendBatch(emails: List<Email>)
    suspend fun sendTemplate(templateId: String, context: Map<String, Any>, to: EmailAddress)
}

data class Email(
    val from: EmailAddress,
    val to: List<EmailAddress>,       // não deve estar vazio
    val subject: String,               // não deve estar em branco
    val content: EmailContent,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val replyTo: EmailAddress? = null,
    val attachments: List<EmailAttachment> = emptyList(),
    val headers: Map<String, String> = emptyMap(),
)

data class EmailContent(
    val html: String? = null,  // ao menos um de html/plain deve ser não-nulo
    val plain: String? = null,
)

data class EmailAddress(
    val address: String,           // deve conter @
    val displayName: String? = null,
)
```

**Decisão de design:** `Email` e `EmailContent` são tipos separados para que `EmailContent` possa carregar variantes `html` e `plain` de forma independente, suportando e-mails MIME multi-part. `EmailAddress` valida o símbolo `@` na construção.
