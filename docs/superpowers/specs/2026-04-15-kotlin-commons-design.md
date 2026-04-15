# kotlin-commons — Design Spec

**Date:** 2026-04-15  
**Status:** Approved  
**Author:** Marcus Prado Silva  

---

## Contexto

O projeto `java-commons` é uma biblioteca de commons multi-módulo (112 módulos) para microserviços, organizada em camadas hexagonais com DDD. O `kotlin-commons` é o equivalente idiomático em Kotlin: mesma arquitetura, mesmas camadas, mas com APIs redesenhadas para aproveitar os recursos da linguagem Kotlin (sealed classes, value classes, coroutines, extension functions).

A implementação começa pelo núcleo (kernel + ports essenciais) e cresce incrementalmente.

---

## Escopo Inicial

### Módulos — Fase 1

```
kotlin-commons/
├── commons-kernel-core/       ← extension functions utilitárias
├── commons-kernel-errors/     ← Problem, ErrorCode, ErrorCategory, Severity, exceções
├── commons-kernel-result/     ← Result<T>, Either<L,R>, Option<T>
├── commons-kernel-ddd/        ← Entity, AggregateRoot, ValueObject, DomainEvent
├── commons-kernel-time/       ← ClockProvider, TimeWindow
├── commons-ports-persistence/ ← Repository, PageRequest, Specification
├── commons-ports-messaging/   ← MessagePublisherPort, MessageConsumerPort
├── commons-ports-http/        ← HttpClientPort (suspend), HttpRequest/Response
├── commons-ports-cache/       ← CachePort
├── commons-ports-email/       ← EmailPort
└── commons-bom/               ← Bill of Materials (Version Catalog)
```

Adapters, Spring starters e demais ports entram em fases subsequentes.

---

## Arquitetura

### Camadas e regras de dependência

```
commons-kernel-*      ← zero dependências externas (sem Spring, sem coroutines, sem Jackson)
      ↑
commons-ports-*       ← depende de kernel + kotlinx-coroutines-core (suspend fun para I/O)
      ↑
commons-adapters-*    ← implementa ports; pode usar libs externas (fase 2+)
      ↑
commons-app-*         ← orquestra ports e adapters (fase 2+)
      ↑
commons-spring-*      ← auto-configuração Spring Boot (fase 2+)
```

**Regra crítica:** o kernel não pode importar nenhuma dependência externa — nem `kotlinx-coroutines-core`. Coroutines entram apenas a partir de `commons-ports-*` para habilitar `suspend fun` nas interfaces de I/O. Extension functions de interop adicionais (ex: `Result.asDeferred()`) ficam nos próprios ports ou em módulo separado.

### Enforcement

ArchUnit valida as regras de dependência por camada — igual ao java-commons. Testes ArchUnit ficam em `commons-testkit-archunit` (fase 2).

---

## Build Infrastructure

### Ferramentas

| Ferramenta              | Versão   | Função                                      |
|-------------------------|----------|---------------------------------------------|
| Kotlin                  | 2.1.0    | Linguagem                                   |
| JVM target              | 21       | Target de bytecode                          |
| Gradle                  | 8.x      | Build tool                                  |
| Kotlin DSL              | —        | `.gradle.kts` em todos os scripts           |
| Version Catalog (TOML)  | —        | Versões centralizadas em `libs.versions.toml` |
| buildSrc                | —        | Convention plugins compartilhados           |

### Convention plugins (buildSrc)

- `kotlin-commons.gradle.kts` — aplicado em todos os módulos:
  - Kotlin 2.1, JVM 21
  - `allWarningsAsErrors = true`
  - `explicitApi()` — toda API pública deve declarar visibilidade
  - Kover para cobertura (60% linha / 55% branch)
  - Kotest como runner de testes
- `kotlin-commons-spring.gradle.kts` — aplicado apenas em módulos com Spring Boot

### Version Catalog (`gradle/libs.versions.toml`)

Versões centralizadas:
- `kotlin = "2.1.0"`
- `coroutines = "1.9.0"`
- `spring-boot = "3.5.0"`
- `kotest = "5.9.1"`
- `mockk = "1.13.12"`

---

## Design das APIs Kotlin

### Value Classes para Value Objects

`value class` no lugar de `SingleValueObject<T>` — sem overhead em runtime:

```kotlin
@JvmInline value class TenantId(val value: String)
@JvmInline value class ActorId(val value: String)
@JvmInline value class EntityVersion(val value: Long)
@JvmInline value class ErrorCode(val value: String)
```

### Result<T> — Sealed Class

Implementado do zero, sem Arrow. Variantes `Ok` e `Fail` como `data class`:

```kotlin
sealed class Result<out T> {
    data class Ok<T>(val value: T) : Result<T>()
    data class Fail(val problem: Problem) : Result<Nothing>()

    // Transformações
    fun <R> map(transform: (T) -> R): Result<R>
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R>
    fun <R> mapError(transform: (Problem) -> Problem): Result<R>

    // Recuperação
    fun getOrElse(default: @UnsafeVariance T): T
    fun getOrElse(default: () -> @UnsafeVariance T): T
    fun recover(transform: (Problem) -> @UnsafeVariance T): Result<T>

    // Folding
    fun <R> fold(onFail: (Problem) -> R, onOk: (T) -> R): R

    // Async (requer coroutines no caller)
    suspend fun <R> mapAsync(transform: suspend (T) -> R): Result<R>
    suspend fun <R> flatMapAsync(transform: suspend (T) -> Result<R>): Result<R>

    // Companion factories
    companion object {
        fun <T> ok(value: T): Result<T> = Ok(value)
        fun <T> fail(problem: Problem): Result<T> = Fail(problem)
    }
}
```

### Either<L, R> e Option<T>

Implementados do zero como `sealed class`, seguindo o mesmo padrão de `Result<T>`.

### Problem — Error Model

```kotlin
data class Problem(
    val code: ErrorCode,
    val category: ErrorCategory,
    val severity: Severity,
    val message: String,
    val details: List<ProblemDetail> = emptyList(),
    val meta: Map<String, Any> = emptyMap(),
    val timestamp: Instant = Instant.now()
)

enum class ErrorCategory {
    VALIDATION, BUSINESS, NOT_FOUND, CONFLICT,
    UNAUTHORIZED, FORBIDDEN, TECHNICAL
}
```

### Hierarquia de Exceções

```kotlin
sealed class DomainException(message: String, val problem: Problem) : RuntimeException(message)
class BusinessException(problem: Problem) : DomainException(problem.message, problem)
class ValidationException(problem: Problem) : DomainException(problem.message, problem)
class NotFoundException(problem: Problem) : DomainException(problem.message, problem)
class ConflictException(problem: Problem) : DomainException(problem.message, problem)
class TechnicalException(problem: Problem) : DomainException(problem.message, problem)
```

### Entity e AggregateRoot (DDD)

```kotlin
abstract class Entity<I : Any>(
    val id: I,
    val tenantId: TenantId,
    var version: EntityVersion,
    var audit: AuditTrail,
    var isDeleted: Boolean = false,
    var deletion: DeletionStamp? = null
) {
    protected fun touch(updated: AuditStamp) { ... }
    protected fun softDelete(stamp: DeletionStamp, updated: AuditStamp) { ... }
    protected fun restore(updated: AuditStamp) { ... }
}

abstract class AggregateRoot<I : Any>(id: I, tenantId: TenantId, version: EntityVersion, audit: AuditTrail)
    : Entity<I>(id, tenantId, version, audit) {

    private val _domainEvents = mutableListOf<DomainEvent>()

    protected fun recordChange(updated: AuditStamp, mutation: () -> Unit, event: (AggregateSnapshot<I>) -> DomainEvent)
    fun pullDomainEvents(): List<DomainEvent>
    fun peekDomainEvents(): List<DomainEvent>
}
```

### Ports de I/O com suspend fun

```kotlin
interface HttpClientPort {
    suspend fun execute(request: HttpRequest): HttpResponse<ByteArray>
    suspend fun <T> execute(request: HttpRequest, mapper: (ByteArray) -> T): HttpResponse<T>
}

interface MessagePublisherPort {
    suspend fun publish(envelope: MessageEnvelope<*>)
    suspend fun publishBatch(envelopes: List<MessageEnvelope<*>>)
}

interface CachePort {
    suspend fun <T> get(key: CacheKey): T?
    suspend fun <T> put(key: CacheKey, value: T, ttl: Duration? = null)
    suspend fun remove(key: CacheKey)
}

interface Repository<E : Any, I : Any> {
    suspend fun findById(id: I): E?          // null em vez de Optional
    suspend fun save(entity: E): E
    suspend fun delete(entity: E)
    suspend fun deleteById(id: I)
}
```

> **Nota:** `Optional` é substituído por nullable `T?` em toda a API — idiomático Kotlin.

### Extension Functions (kernel-core)

Em vez de classes estáticas (`Strings.java`, `Uuids.java`):

```kotlin
// strings.kt
fun String.toSlug(): String
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String
fun String?.nullIfBlank(): String?

// uuids.kt
fun randomUuid(): UUID = UUID.randomUUID()
fun String.toUuid(): UUID = UUID.fromString(this)
fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
```

---

## Testes

### Stack

| Java (java-commons)  | Kotlin (kotlin-commons)         |
|----------------------|---------------------------------|
| JUnit 5              | Kotest (FunSpec / BehaviorSpec) |
| AssertJ              | Kotest Matchers                 |
| Mockito              | MockK                           |
| Testcontainers       | Testcontainers-Kotlin           |
| JaCoCo               | Kover                           |
| ArchUnit             | ArchUnit (funciona em Kotlin)   |

### Padrão de teste

```kotlin
class ResultTest : FunSpec({
    test("ok should hold value") {
        val result = Result.ok("hello")
        result.isOk() shouldBe true
        result.getOrElse { "" } shouldBe "hello"
    }

    test("map transforms ok value") {
        Result.ok("hello").map { it.uppercase() } shouldBe Result.ok("HELLO")
    }

    test("mapAsync runs in coroutine context") {
        Result.ok(42).mapAsync { delay(1); it * 2 } shouldBe Result.ok(84)
    }
})
```

### Cobertura mínima (Kover)

- 60% linha
- 55% branch

---

## Qualidade de código

| Ferramenta     | Equivalente Java         | Função                                    |
|----------------|--------------------------|-------------------------------------------|
| ktlint         | Checkstyle + Google Style| Formatação e estilo Kotlin                |
| detekt         | SpotBugs + PMD           | Análise estática, code smells             |
| Kover          | JaCoCo                   | Cobertura com suporte a inline/coroutines |
| `explicitApi()`| —                        | Obriga visibilidade explícita em APIs     |

---

## Decisões de design relevantes

1. **Sem Arrow no kernel** — `Result`, `Either`, `Option` implementados do zero; mantém kernel sem dependências externas.
2. **`T?` em vez de `Optional<T>`** — idiomatic Kotlin; `Optional` não é usado em nenhuma API pública.
3. **`value class` para Value Objects** — zero custo em runtime, verificação de tipo em compile time.
4. **`suspend fun` nas ports de I/O** — integração natural com coroutines; adapters implementam com a lib de sua escolha.
5. **`explicitApi()`** — toda API pública declara `public`/`internal` explicitamente; previne exposição acidental.
6. **buildSrc com convention plugins** — evita repetição de configuração Gradle em 10+ módulos.
7. **Version Catalog TOML** — substitui o BOM Maven como fonte única de verdade para versões.

---

## Fora do escopo (Fase 1)

- Adapters (JPA, Redis, Kafka, OkHttp, etc.)
- Spring Boot starters
- commons-app-* (resiliência, idempotência, outbox, etc.)
- Ports secundários (SMS, PDF, Excel, blockchain, ML, etc.)
- Módulo de interop com java-commons
