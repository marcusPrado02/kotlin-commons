# Kernel

A camada kernel contém módulos Kotlin puros sem dependências de framework ou infraestrutura. São os blocos de construção que qualquer camada de serviço pode importar com segurança.

---

## kernel-core

**O que é:** Extensões utilitárias para coleções e verificações de pré-condição. Sem lógica de domínio — apenas helpers puros.

**Quando usar:** Sempre que precisar de acesso seguro a listas, transformações de lista ou entrada validada nas fronteiras do sistema.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.core.*

// Coleções
listOf(1, 2, 3).secondOrNull()              // 2
listOf(1, 2, 3).updated(1) { it * 10 }     // [1, 20, 3]
mapOf("a" to 1).mergeWith(mapOf("b" to 2)) // {"a":1,"b":2}
listOf(1, 2, 3, 1, 2).splitWhen { it == 1 } // [[1,2,3],[1,2]]

// Pré-condições — lança IllegalArgumentException em violação
requireNotBlank("hello")   // retorna "hello"
requireNotBlank("  ")      // lança IllegalArgumentException
requirePositive(5)         // retorna 5
requirePositive(-1)        // lança IllegalArgumentException
```

**Decisão de design:** `requireNotBlank` e `requirePositive` produzem mensagens de erro consistentes e legíveis sem que o chamador precise escrever a mensagem. Eles validam nos pontos de entrada do sistema e permitem que o restante do código assuma dados válidos.

---

## kernel-result

**O que é:** `Result<T>`, `Either<L, R>` e `Option<T>` — tipos algébricos para tratamento de erros sem exceções.

**Quando usar:** Use `Result<T>` como tipo de retorno de toda operação que pode falhar. Use `Option<T>` para valores que podem estar ausentes. Use `Either<L, R>` quando esquerda e direita têm significados independentes além de erro/sucesso.

**API pública principal — Result<T>:**

```kotlin
import com.marcusprado02.commons.kernel.result.*

// Construção
val ok: Result<Int>  = Result.ok(42)
val err: Result<Int> = Result.fail(Problems.notFound(ErrorCode("X"), "não encontrado"))

// Transformação — aplica-se apenas quando Ok
ok.map { it * 2 }             // Result.ok(84)
err.map { it * 2 }            // Result.fail(...) inalterado

// Encadeamento
ok.flatMap { n ->
    if (n > 0) Result.ok(n)
    else Result.fail(Problems.validation(ErrorCode("NEG"), "deve ser positivo"))
}

// Combinando dois Results — ambos devem ter sucesso
val a = Result.ok(1)
val b = Result.ok(2)
a.zipWith(b) { x, y -> x + y }  // Result.ok(3)

// Coletando uma lista — falha no primeiro erro
val results = listOf(Result.ok(1), Result.ok(2), Result.ok(3))
Result.sequence(results)          // Result.ok([1, 2, 3])

// Recuperando de erro
err.recover { problem -> 0 }      // Result.ok(0)

// Consumindo ambos os lados
ok.fold(
    onFail = { problem -> "erro: ${problem.message}" },
    onOk   = { value   -> "valor: $value" }
)
```

**API pública principal — Either<L, R>:**

```kotlin
// Construção
val left:  Either<String, Int> = Either.left("mensagem de erro")
val right: Either<String, Int> = Either.right(42)

// Fold
right.fold(
    onLeft  = { msg   -> "falhou: $msg" },
    onRight = { value -> "ok: $value" }
)

// Mapeamento
right.mapRight { it * 2 }          // Either.right(84)
left.mapLeft  { it.uppercase() }   // Either.left("MENSAGEM DE ERRO")
```

**API pública principal — Option<T>:**

```kotlin
// Construção
val some: Option<String> = Option.some("hello")
val none: Option<String> = Option.none()
val fromNullable: Option<String> = "hello".toOption()
val fromNull: Option<String> = null.toOption()       // None

// Extração
some.getOrElse("padrão")   // "hello"
none.getOrElse("padrão")   // "padrão"

// Transformando
some.map { it.uppercase() }  // Some("HELLO")
none.map { it.uppercase() }  // None

// Convertendo para Result
some.toResult(Problems.notFound(ErrorCode("AUSENTE"), "não encontrado")) // Result.ok("hello")
none.toResult(Problems.notFound(ErrorCode("AUSENTE"), "não encontrado")) // Result.fail(problem)
```

**Decisão de design:** `Result<T>` é preferido ao `kotlin.Result` porque: (1) `kotlin.Result` só aceita `Throwable`; (2) nosso `Result` carrega diretamente um objeto `Problem` estruturado; (3) a API fold (`onFail`/`onOk`) é explícita sobre qual ramo trata qual caso. `Either<L, R>` é usado quando ambos os lados carregam valores de domínio significativos.

---

## kernel-errors

**O que é:** Representação estruturada de erros: `Problem` (valor serializável), `Problems` (factory), `ErrorCode`, `ErrorCategory`, `Severity` e hierarquia sealed `DomainException`.

**Quando usar:** Retorne `Problem` de operações de domínio; lance subclasses de `DomainException` apenas nas fronteiras com frameworks baseados em exceção.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.errors.*

val p1 = Problems.notFound(ErrorCode("USER_NOT_FOUND"), "Usuário 42 não encontrado")
val p2 = Problems.validation(ErrorCode("EMAIL_INVALIDO"), "E-mail inválido",
             ProblemDetail("email", "deve ser válido"))
val p3 = Problems.conflict(ErrorCode("EMAIL_DUPLICADO"), "E-mail já cadastrado")
val p4 = Problems.unauthorized(ErrorCode("TOKEN_EXPIRADO"), "Token expirado")
val p5 = Problems.forbidden(ErrorCode("ACESSO_NEGADO"), "Permissões insuficientes")
val p6 = Problems.business(ErrorCode("LIMITE_PEDIDO"), "Limite de pedidos excedido")
val p7 = Problems.technical(ErrorCode("DB_INDISPONIVEL"), "Banco de dados indisponível")

// Adicionando metadados de contexto
val comContexto = p1.withContext("requestId", "abc-123")
                    .withContext("userId", "42")

// Convertendo exceções em problems
val p = Problems.fromException(IllegalArgumentException("entrada inválida"))
// p.category == ErrorCategory.VALIDATION

// Subclasses de DomainException — lance apenas nas fronteiras de framework
throw NotFoundException(p1)
throw ValidationException(p2)
throw BusinessException(p6, cause = excecaoOriginal)

// Problem é @Serializable — seguro para retornar em respostas HTTP
val json = Json.encodeToString(p1)
val decoded = Json.decodeFromString<Problem>(json)
```

**Categorias e severidades padrão:**

| Categoria | Severidade | Status HTTP sugerido |
|---|---|---|
| NOT_FOUND | LOW | 404 |
| VALIDATION | LOW | 400 |
| CONFLICT | MEDIUM | 409 |
| UNAUTHORIZED | HIGH | 401 |
| FORBIDDEN | HIGH | 403 |
| BUSINESS | MEDIUM | 422 |
| TECHNICAL | CRITICAL | 500 |

**Decisão de design:** `Problem` é `data class` com `@Serializable` para serialização direta em respostas de API. `DomainException` é sealed para que o compilador exija tratamento exaustivo nos blocos `catch`. `ErrorCode` valida na construção para que erros apareçam cedo.

---

## kernel-ddd

**O que é:** Blocos DDD leves: `Command`/`CommandHandler`, `Query`/`QueryHandler`, `DomainEvent`, `ValueObject`, `AggregateRoot`.

**Quando usar:** Use `CommandHandler` e `QueryHandler` para organizar casos de uso da camada de aplicação. Use `AggregateRoot` e `DomainEvent` para agregados que publicam eventos de domínio. Use `ValueObject` para reforçar invariantes em identificadores de domínio.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.ddd.*
import com.marcusprado02.commons.kernel.result.*

// Comandos e handlers
data class CriarUsuarioCommand(val email: String, val nome: String) : Command
data class ResultadoCriarUsuario(val id: String)

class CriarUsuarioHandler(
    private val repo: UserRepository
) : CommandHandler<CriarUsuarioCommand, Result<ResultadoCriarUsuario>> {
    override suspend fun handle(command: CriarUsuarioCommand): Result<ResultadoCriarUsuario> {
        requireNotBlank(command.email)
        return Result.ok(ResultadoCriarUsuario(id = "id-gerado"))
    }
}

// Value objects
@JvmInline
value class UserId(val value: String) : ValueObject {
    init { requireNotBlank(value) }
}

// Agregados e eventos de domínio
class PedidoCriadoEvento(val pedidoId: String) : DomainEvent

class Pedido(id: PedidoId) : AggregateRoot<PedidoId>(id) {
    fun fazer(): Pedido {
        registerEvent(PedidoCriadoEvento(id.value))
        return this
    }
}
// pedido.domainEvents contém todos os eventos registrados após fazer()
```

**Decisão de design:** `CommandHandler` e `QueryHandler` são interfaces para que uma única classe possa implementar múltiplos handlers. `AggregateRoot` armazena eventos de domínio em uma lista para dispatch pela camada de aplicação após commit bem-sucedido, evitando acoplamento transacional.

---

## kernel-time

**O que é:** Interface `ClockProvider` com implementações de sistema e fixa, mais `TimeWindow` para aritmética de intervalos.

**Quando usar:** Injete `ClockProvider` em vez de chamar `Instant.now()` diretamente. Use `FixedClockProvider` em testes para tornar o tempo determinístico.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.time.*

// Produção
val clock: ClockProvider = SystemClockProvider()
val agora: Instant = clock.now()

// Testes — tempo determinístico
val fixo: ClockProvider = FixedClockProvider(Instant.parse("2024-01-01T00:00:00Z"))
fixo.now() // sempre retorna 2024-01-01T00:00:00Z

// Janelas de tempo
val janela = TimeWindow(
    start = Instant.parse("2024-01-01T00:00:00Z"),
    end   = Instant.parse("2024-01-31T23:59:59Z")
)
janela.contains(Instant.parse("2024-01-15T12:00:00Z")) // true
janela.contains(Instant.parse("2024-02-01T00:00:00Z")) // false
```

**Decisão de design:** `ClockProvider` é uma interface para que classes que precisam do tempo atual declarem essa dependência explicitamente, tornando-a visível no construtor e permitindo testes determinísticos sem mock de `Instant`.
