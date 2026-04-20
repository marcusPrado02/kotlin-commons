# kotlin-commons

![CI](https://github.com/marcusprado02/kotlin-commons/actions/workflows/ci.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg)
![JVM](https://img.shields.io/badge/JVM-21-orange.svg)
![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)

## Usage Examples

### commons-kernel-result — Result composition

```kotlin
val a: Result<Problem, Int> = Result.success(10)
val b: Result<Problem, Int> = Result.success(32)

// Combine two results into a pair
val sum: Result<Problem, Int> = a.zip(b) { x, y -> x + y }

// Collect a list of results, failing on the first error
val all: Result<Problem, List<Int>> = Result.sequence(listOf(a, b))
```

### commons-kernel-errors — Problem construction

```kotlin
// Map a JVM exception to a domain Problem
val problem: Problem = Problems.fromException(illegalArgumentException)

// Immutably enrich a problem with contextual metadata
val detailed: Problem = problem.withContext("orderId", "ORD-42")
    .withContext("customerId", "CUST-7")
```

### commons-kernel-ddd — CQRS Command/CommandHandler

```kotlin
data class PlaceOrderCommand(val orderId: String, val amount: BigDecimal) : Command

class PlaceOrderHandler : CommandHandler<PlaceOrderCommand, Result<Problem, Order>> {
    override fun handle(command: PlaceOrderCommand): Result<Problem, Order> {
        // domain logic here
        return Result.success(Order(command.orderId))
    }
}
```

### commons-ports-cache — CachePort.getOrPut

```kotlin
val cache: CachePort<String, UserProfile> = // injected

val profile: UserProfile = cache.getOrPut(key = "user:42", ttl = 5.minutes) {
    userRepository.findById("42") ?: error("User not found")
}
```

### commons-ports-http — HttpClientPort convenience extensions

```kotlin
val client: HttpClientPort = // injected

val response: HttpResponse = client.get("https://api.example.com/v1/products")

val created: HttpResponse = client.post(
    uri = "https://api.example.com/v1/orders",
    body = HttpBody.Json(newOrder),
)
```
