# Kernel

The kernel layer contains pure Kotlin modules with no framework or infrastructure dependencies. These are the building blocks every service layer can safely import.

---

## kernel-core

**What it is:** Utility extensions for collections and precondition checks. No domain logic — pure helpers.

**When to use:** Wherever you need safe list access, list transformations, or validated input at system boundaries.

**Key public API:**

```kotlin
import com.marcusprado02.commons.kernel.core.*

// Collections
listOf(1, 2, 3).secondOrNull()              // 2
listOf(1, 2, 3).updated(1) { it * 10 }     // [1, 20, 3]
mapOf("a" to 1).mergeWith(mapOf("b" to 2)) // {"a":1,"b":2}
listOf(1, 2, 3, 1, 2).splitWhen { it == 1 } // [[1,2,3],[1,2]]

// Preconditions — throw IllegalArgumentException on violation
requireNotBlank("hello")   // returns "hello"
requireNotBlank("  ")      // throws IllegalArgumentException("must not be blank")
requirePositive(5)         // returns 5
requirePositive(-1)        // throws IllegalArgumentException("must be positive")
```

**Design decision:** `requireNotBlank` and `requirePositive` are chosen over Kotlin's built-in `require` because they produce consistent, readable error messages without requiring the caller to write the message string. They validate at system entry points (controllers, command handlers) and let the rest of the code assume valid data.

---

## kernel-result

**What it is:** `Result<T>`, `Either<L, R>`, and `Option<T>` — algebraic data types for error handling without exceptions.

**When to use:** Use `Result<T>` as the return type of every operation that can fail. Use `Option<T>` for values that may be absent. Use `Either<L, R>` when left and right have independent meanings beyond error/success.

**Key public API — Result<T>:**

```kotlin
import com.marcusprado02.commons.kernel.result.*

// Construction
val ok: Result<Int>  = Result.ok(42)
val err: Result<Int> = Result.fail(Problems.notFound(ErrorCode("X"), "not found"))

// Transformation — only applies when Ok
ok.map { it * 2 }             // Result.ok(84)
err.map { it * 2 }            // Result.fail(...) unchanged

// Chaining — flatMap for operations that also return Result
ok.flatMap { n ->
    if (n > 0) Result.ok(n)
    else Result.fail(Problems.validation(ErrorCode("NEG"), "must be positive"))
}

// Combining two Results — both must succeed
val a = Result.ok(1)
val b = Result.ok(2)
a.zipWith(b) { x, y -> x + y }  // Result.ok(3)

// Collecting a list — fails on first error
val results = listOf(Result.ok(1), Result.ok(2), Result.ok(3))
Result.sequence(results)          // Result.ok([1, 2, 3])

// Recovering from error
err.recover { problem -> 0 }      // Result.ok(0)

// Consuming both sides
ok.fold(
    onFail = { problem -> "error: ${problem.message}" },
    onOk   = { value   -> "value: $value" }
)
```

**Key public API — Either<L, R>:**

```kotlin
// Construction
val left:  Either<String, Int> = Either.left("error message")
val right: Either<String, Int> = Either.right(42)

// Fold
right.fold(
    onLeft  = { msg   -> "failed: $msg" },
    onRight = { value -> "ok: $value" }
)

// Mapping — mapRight applies to Right, mapLeft applies to Left
right.mapRight { it * 2 }          // Either.right(84)
left.mapLeft  { it.uppercase() }   // Either.left("ERROR MESSAGE")

// Interop with Result
val result: Result<Int> = right.toResult() // where right is Either<Problem, Int>
```

**Key public API — Option<T>:**

```kotlin
// Construction
val some: Option<String> = Option.some("hello")
val none: Option<String> = Option.none()
val fromNullable: Option<String> = "hello".toOption() // Some("hello")
val fromNull: Option<String> = null.toOption()        // None

// Extracting
some.getOrElse("default")   // "hello"
none.getOrElse("default")   // "default"
some.getOrNull()             // "hello"
none.getOrNull()             // null

// Transforming
some.map { it.uppercase() }  // Some("HELLO")
none.map { it.uppercase() }  // None

some.filter { it.length > 3 }  // Some("hello")
some.filter { it.length > 10 } // None

// Converting to Result
some.toResult(Problems.notFound(ErrorCode("MISSING"), "not found")) // Result.ok("hello")
none.toResult(Problems.notFound(ErrorCode("MISSING"), "not found")) // Result.fail(problem)
```

**Design decision:** `Result<T>` is preferred over `kotlin.Result` because: (1) `kotlin.Result` only wraps `Throwable`, forcing exceptions as values; (2) our `Result` carries a structured `Problem` object directly; (3) the fold API (`onFail`/`onOk`) is explicit about which branch handles which case, reducing confusion at call sites. `Either<L, R>` is used when both sides carry meaningful domain values (not just error/success).

---

## kernel-errors

**What it is:** Structured error representation: `Problem` (serializable error value), `Problems` factory object, `ErrorCode`, `ErrorCategory`, `Severity`, and a sealed `DomainException` hierarchy.

**When to use:** Return `Problem` from domain operations; throw `DomainException` subclasses only at boundaries where you must integrate with exception-based frameworks.

**Key public API:**

```kotlin
import com.marcusprado02.commons.kernel.errors.*

// Creating problems via factory
val p1 = Problems.notFound(ErrorCode("USER_NOT_FOUND"), "User 42 not found")
val p2 = Problems.validation(ErrorCode("INVALID_EMAIL"), "Invalid email",
             ProblemDetail("email", "must be valid"))
val p3 = Problems.conflict(ErrorCode("DUPLICATE_EMAIL"), "Email already registered")
val p4 = Problems.unauthorized(ErrorCode("TOKEN_EXPIRED"), "Token has expired")
val p5 = Problems.forbidden(ErrorCode("ACCESS_DENIED"), "Insufficient permissions")
val p6 = Problems.business(ErrorCode("ORDER_LIMIT"), "Order limit exceeded")
val p7 = Problems.technical(ErrorCode("DB_UNAVAILABLE"), "Database unavailable")

// Adding context metadata
val traced = p1.withContext("requestId", "abc-123")
                .withContext("userId", "42")

// Converting exceptions to problems
val p = Problems.fromException(IllegalArgumentException("bad input"))
// p.category == ErrorCategory.VALIDATION

// ErrorCode rejects blank strings
ErrorCode("")    // throws IllegalArgumentException
ErrorCode("   ") // throws IllegalArgumentException

// DomainException subclasses — throw only at framework boundaries
throw NotFoundException(p1)
throw ValidationException(p2)
throw BusinessException(p6, cause = originalException)

// Problem is @Serializable — safe to return over HTTP
val json = Json.encodeToString(p1)
val decoded = Json.decodeFromString<Problem>(json)
```

**Categories and default severities:**

| Category | Severity | HTTP status (suggested) |
|---|---|---|
| NOT_FOUND | LOW | 404 |
| VALIDATION | LOW | 400 |
| CONFLICT | MEDIUM | 409 |
| UNAUTHORIZED | HIGH | 401 |
| FORBIDDEN | HIGH | 403 |
| BUSINESS | MEDIUM | 422 |
| TECHNICAL | CRITICAL | 500 |

**Design decision:** `Problem` is a `data class` annotated with `@Serializable` so it can be serialised directly into API responses without a DTO conversion step. `DomainException` is sealed so the compiler enforces exhaustive handling at `catch` sites. `ErrorCode` validates at construction time rather than at serialisation time so errors surface early.

---

## kernel-ddd

**What it is:** Lightweight DDD building blocks: `Command`/`CommandHandler`, `Query`/`QueryHandler`, `DomainEvent`, `ValueObject`, `AggregateRoot`.

**When to use:** Use `CommandHandler` and `QueryHandler` to organise application-layer use cases. Use `AggregateRoot` and `DomainEvent` for aggregates that publish domain events. Use `ValueObject` to enforce invariants on domain identifiers and small value types.

**Key public API:**

```kotlin
import com.marcusprado02.commons.kernel.ddd.*
import com.marcusprado02.commons.kernel.errors.*
import com.marcusprado02.commons.kernel.result.*

// Commands and handlers
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

// Queries and handlers
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

// Aggregates and domain events
class OrderCreatedEvent(val orderId: String) : DomainEvent

class Order(id: OrderId) : AggregateRoot<OrderId>(id) {
    fun place(): Order {
        registerEvent(OrderCreatedEvent(id.value))
        return this
    }
}
// order.domainEvents contains all registered events after place()
```

**Design decision:** `CommandHandler` and `QueryHandler` are interfaces (not abstract classes) so a single class can implement multiple handlers when co-location makes the code clearer. `AggregateRoot` stores domain events in a list and exposes `domainEvents` for the application layer to dispatch after a successful commit — this keeps event publishing outside the domain model, avoiding transactional coupling.

---

## kernel-time

**What it is:** `ClockProvider` interface with system and fixed implementations, plus `TimeWindow` for interval arithmetic.

**When to use:** Inject `ClockProvider` instead of calling `Instant.now()` directly. Use `FixedClockProvider` in tests to make time deterministic.

**Key public API:**

```kotlin
import com.marcusprado02.commons.kernel.time.*

// Production wiring
val clock: ClockProvider = SystemClockProvider()
val now: Instant = clock.now()

// Test wiring — deterministic time
val fixed: ClockProvider = FixedClockProvider(Instant.parse("2024-01-01T00:00:00Z"))
fixed.now() // always returns 2024-01-01T00:00:00Z

// Time windows
val window = TimeWindow(
    start = Instant.parse("2024-01-01T00:00:00Z"),
    end   = Instant.parse("2024-01-31T23:59:59Z")
)
window.contains(Instant.parse("2024-01-15T12:00:00Z")) // true
window.contains(Instant.parse("2024-02-01T00:00:00Z")) // false
```

**Design decision:** `ClockProvider` is an interface rather than a global function so that classes that need the current time declare an explicit dependency on it. This makes the dependency visible in the constructor, enables deterministic testing without mocking `Instant`, and avoids hidden coupling to system time in domain logic.
