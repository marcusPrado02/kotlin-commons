# Kernel

La capa kernel contiene módulos Kotlin puros sin dependencias de framework o infraestructura. Estos son los bloques de construcción que cualquier capa de servicio puede importar con seguridad.

---

## kernel-core

**Qué es:** Extensiones de utilidad para colecciones y verificaciones de precondiciones. Sin lógica de dominio — solo helpers puros.

**Cuándo usarlo:** En cualquier lugar donde necesites acceso seguro a listas, transformaciones de listas o validación de entradas en los límites del sistema.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.core.*

// Colecciones
listOf(1, 2, 3).secondOrNull()              // 2
listOf(1, 2, 3).updated(1) { it * 10 }     // [1, 20, 3]
mapOf("a" to 1).mergeWith(mapOf("b" to 2)) // {"a":1,"b":2}
listOf(1, 2, 3, 1, 2).splitWhen { it == 1 } // [[1,2,3],[1,2]]

// Precondiciones — lanza IllegalArgumentException si se viola
requireNotBlank("hello")   // retorna "hello"
requireNotBlank("  ")      // lanza IllegalArgumentException("must not be blank")
requirePositive(5)         // retorna 5
requirePositive(-1)        // lanza IllegalArgumentException("must be positive")
```

**Decisión de diseño:** `requireNotBlank` y `requirePositive` se prefieren sobre el `require` integrado de Kotlin porque producen mensajes de error consistentes y legibles sin que el llamador tenga que escribir el texto del mensaje. Validan en los puntos de entrada del sistema (controladores, manejadores de comandos) y permiten que el resto del código asuma datos válidos.

---

## kernel-result

**Qué es:** `Result<T>`, `Either<L, R>` y `Option<T>` — tipos de datos algebraicos para el manejo de errores sin excepciones.

**Cuándo usarlo:** Usa `Result<T>` como tipo de retorno de toda operación que pueda fallar. Usa `Option<T>` para valores que pueden estar ausentes. Usa `Either<L, R>` cuando el lado izquierdo y el derecho tienen significados independientes más allá de error/éxito.

**API pública principal — Result<T>:**

```kotlin
import com.marcusprado02.commons.kernel.result.*

// Construcción
val ok: Result<Int>  = Result.ok(42)
val err: Result<Int> = Result.fail(Problems.notFound(ErrorCode("X"), "not found"))

// Transformación — solo se aplica cuando Ok
ok.map { it * 2 }             // Result.ok(84)
err.map { it * 2 }            // Result.fail(...) sin cambios

// Encadenamiento — flatMap para operaciones que también retornan Result
ok.flatMap { n ->
    if (n > 0) Result.ok(n)
    else Result.fail(Problems.validation(ErrorCode("NEG"), "must be positive"))
}

// Combinando dos Results — ambos deben tener éxito
val a = Result.ok(1)
val b = Result.ok(2)
a.zipWith(b) { x, y -> x + y }  // Result.ok(3)

// Recolectando una lista — falla en el primer error
val results = listOf(Result.ok(1), Result.ok(2), Result.ok(3))
Result.sequence(results)          // Result.ok([1, 2, 3])

// Recuperando de un error
err.recover { problem -> 0 }      // Result.ok(0)

// Consumiendo ambos lados
ok.fold(
    onFail = { problem -> "error: ${problem.message}" },
    onOk   = { value   -> "value: $value" }
)
```

**API pública principal — Either<L, R>:**

```kotlin
// Construcción
val left:  Either<String, Int> = Either.left("error message")
val right: Either<String, Int> = Either.right(42)

// Fold
right.fold(
    onLeft  = { msg   -> "failed: $msg" },
    onRight = { value -> "ok: $value" }
)

// Mapeo — mapRight se aplica a Right, mapLeft se aplica a Left
right.mapRight { it * 2 }          // Either.right(84)
left.mapLeft  { it.uppercase() }   // Either.left("ERROR MESSAGE")

// Interoperabilidad con Result
val result: Result<Int> = right.toResult() // where right is Either<Problem, Int>
```

**API pública principal — Option<T>:**

```kotlin
// Construcción
val some: Option<String> = Option.some("hello")
val none: Option<String> = Option.none()
val fromNullable: Option<String> = "hello".toOption() // Some("hello")
val fromNull: Option<String> = null.toOption()        // None

// Extracción
some.getOrElse("default")   // "hello"
none.getOrElse("default")   // "default"
some.getOrNull()             // "hello"
none.getOrNull()             // null

// Transformación
some.map { it.uppercase() }  // Some("HELLO")
none.map { it.uppercase() }  // None

some.filter { it.length > 3 }  // Some("hello")
some.filter { it.length > 10 } // None

// Convirtiendo a Result
some.toResult(Problems.notFound(ErrorCode("MISSING"), "not found")) // Result.ok("hello")
none.toResult(Problems.notFound(ErrorCode("MISSING"), "not found")) // Result.fail(problem)
```

**Decisión de diseño:** `Result<T>` se prefiere sobre `kotlin.Result` porque: (1) `kotlin.Result` solo envuelve `Throwable`, forzando excepciones como valores; (2) nuestro `Result` lleva un objeto `Problem` estructurado directamente; (3) la API fold (`onFail`/`onOk`) es explícita sobre qué rama maneja cada caso, reduciendo la confusión en los puntos de llamada. `Either<L, R>` se usa cuando ambos lados llevan valores de dominio significativos (no solo error/éxito).

---

## kernel-errors

**Qué es:** Representación estructurada de errores: `Problem` (valor de error serializable), el objeto factory `Problems`, `ErrorCode`, `ErrorCategory`, `Severity` y una jerarquía sellada `DomainException`.

**Cuándo usarlo:** Retorna `Problem` desde operaciones de dominio; lanza subclases de `DomainException` solo en límites donde debes integrarte con frameworks basados en excepciones.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.errors.*

// Creación de problemas vía factory
val p1 = Problems.notFound(ErrorCode("USER_NOT_FOUND"), "User 42 not found")
val p2 = Problems.validation(ErrorCode("INVALID_EMAIL"), "Invalid email",
             ProblemDetail("email", "must be valid"))
val p3 = Problems.conflict(ErrorCode("DUPLICATE_EMAIL"), "Email already registered")
val p4 = Problems.unauthorized(ErrorCode("TOKEN_EXPIRED"), "Token has expired")
val p5 = Problems.forbidden(ErrorCode("ACCESS_DENIED"), "Insufficient permissions")
val p6 = Problems.business(ErrorCode("ORDER_LIMIT"), "Order limit exceeded")
val p7 = Problems.technical(ErrorCode("DB_UNAVAILABLE"), "Database unavailable")

// Agregando metadatos de contexto
val traced = p1.withContext("requestId", "abc-123")
                .withContext("userId", "42")

// Convirtiendo excepciones en problemas
val p = Problems.fromException(IllegalArgumentException("bad input"))
// p.category == ErrorCategory.VALIDATION

// ErrorCode rechaza cadenas en blanco
ErrorCode("")    // lanza IllegalArgumentException
ErrorCode("   ") // lanza IllegalArgumentException

// Subclases de DomainException — lanzar solo en fronteras de framework
throw NotFoundException(p1)
throw ValidationException(p2)
throw BusinessException(p6, cause = originalException)

// Problem es @Serializable — seguro para retornar en respuestas HTTP
val json = Json.encodeToString(p1)
val decoded = Json.decodeFromString<Problem>(json)
```

**Categorías y severidades predeterminadas:**

| Categoría | Severidad | Estado HTTP (sugerido) |
|---|---|---|
| NOT_FOUND | LOW | 404 |
| VALIDATION | LOW | 400 |
| CONFLICT | MEDIUM | 409 |
| UNAUTHORIZED | HIGH | 401 |
| FORBIDDEN | HIGH | 403 |
| BUSINESS | MEDIUM | 422 |
| TECHNICAL | CRITICAL | 500 |

**Decisión de diseño:** `Problem` es una `data class` anotada con `@Serializable` para que pueda serializarse directamente en respuestas de API sin un paso de conversión a DTO. `DomainException` es sellada para que el compilador fuerce el manejo exhaustivo en los sitios `catch`. `ErrorCode` valida en el momento de construcción en lugar de en la serialización, para que los errores aparezcan temprano.

---

## kernel-ddd

**Qué es:** Bloques de construcción DDD ligeros: `Command`/`CommandHandler`, `Query`/`QueryHandler`, `DomainEvent`, `ValueObject`, `AggregateRoot`.

**Cuándo usarlo:** Usa `CommandHandler` y `QueryHandler` para organizar los casos de uso de la capa de aplicación. Usa `AggregateRoot` y `DomainEvent` para agregados que publican eventos de dominio. Usa `ValueObject` para hacer cumplir invariantes en identificadores de dominio y tipos de valor pequeños.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.ddd.*
import com.marcusprado02.commons.kernel.errors.*
import com.marcusprado02.commons.kernel.result.*

// Comandos y handlers
data class CreateUserCommand(val email: String, val name: String) : Command
data class CreateUserResult(val id: String)

class CreateUserHandler(
    private val repo: UserRepository
) : CommandHandler<CreateUserCommand, Result<CreateUserResult>> {
    override suspend fun handle(command: CreateUserCommand): Result<CreateUserResult> {
        requireNotBlank(command.email)
        // ... domain logic
        return Result.ok(CreateUserResult(id = "generated-id"))
    }
}

// Queries y handlers
data class GetUserQuery(val id: String) : Query
class GetUserHandler(private val repo: UserRepository) : QueryHandler<GetUserQuery, Result<User>> {
    override suspend fun handle(query: GetUserQuery): Result<User> =
        repo.findById(UserId(query.id))
}

// Value objects
@JvmInline
value class UserId(val value: String) : ValueObject {
    init { requireNotBlank(value) }
}

// Agregados y eventos de dominio
class OrderCreatedEvent(val orderId: String) : DomainEvent

class Order(id: OrderId) : AggregateRoot<OrderId>(id) {
    fun place(): Order {
        registerEvent(OrderCreatedEvent(id.value))
        return this
    }
}
// order.domainEvents contiene todos los eventos registrados después de place()
```

**Decisión de diseño:** `CommandHandler` y `QueryHandler` son interfaces (no clases abstractas) para que una sola clase pueda implementar múltiples manejadores cuando agruparlos hace que el código sea más claro. `AggregateRoot` almacena eventos de dominio en una lista y expone `domainEvents` para que la capa de aplicación los despache tras un commit exitoso — esto mantiene la publicación de eventos fuera del modelo de dominio, evitando el acoplamiento transaccional.

---

## kernel-time

**Qué es:** La interfaz `ClockProvider` con implementaciones de sistema y fijas, más `TimeWindow` para aritmética de intervalos.

**Cuándo usarlo:** Inyecta `ClockProvider` en lugar de llamar a `Instant.now()` directamente. Usa `FixedClockProvider` en pruebas para hacer el tiempo determinista.

**API pública principal:**

```kotlin
import com.marcusprado02.commons.kernel.time.*

// Configuración en producción
val clock: ClockProvider = SystemClockProvider()
val now: Instant = clock.now()

// Configuración en pruebas — tiempo determinístico
val fixed: ClockProvider = FixedClockProvider(Instant.parse("2024-01-01T00:00:00Z"))
fixed.now() // always returns 2024-01-01T00:00:00Z

// Ventanas de tiempo
val window = TimeWindow(
    start = Instant.parse("2024-01-01T00:00:00Z"),
    end   = Instant.parse("2024-01-31T23:59:59Z")
)
window.contains(Instant.parse("2024-01-15T12:00:00Z")) // true
window.contains(Instant.parse("2024-02-01T00:00:00Z")) // false
```

**Decisión de diseño:** `ClockProvider` es una interfaz en lugar de una función global para que las clases que necesitan la hora actual declaren una dependencia explícita. Esto hace que la dependencia sea visible en el constructor, permite pruebas deterministas sin mockear `Instant` y evita el acoplamiento oculto al tiempo del sistema en la lógica de dominio.
