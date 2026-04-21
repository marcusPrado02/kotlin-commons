# Primeros Pasos

kotlin-commons es un conjunto modular de bibliotecas Kotlin para servicios backend JVM en producción. Está organizado en tres capas: **kernel** (primitivos de dominio puro sin dependencias de framework), **ports** (contratos de interfaz para infraestructura) y **adapters** (implementaciones basadas en bibliotecas populares).

La biblioteca existe porque las preocupaciones transversales comunes — manejo tipado de errores, bloques de construcción DDD y abstracciones de infraestructura — no deben reinventarse en cada servicio. kotlin-commons los provee como módulos pequeños y enfocados que pueden adoptarse de forma individual o en conjunto.

---

## Requisitos previos

- JVM 21+
- Kotlin 2.1.0+
- Gradle 8+ o Maven 3.9+

---

## Instalación

### Gradle (recomendado — usar el BOM)

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))

    // elige solo los módulos que necesitas
    implementation("io.github.marcusprado02.commons:commons-kernel-result")
    implementation("io.github.marcusprado02.commons:commons-kernel-errors")
    implementation("io.github.marcusprado02.commons:commons-ports-cache")
    implementation("io.github.marcusprado02.commons:commons-adapters-cache-redis")

    // helpers de prueba
    testImplementation("io.github.marcusprado02.commons:commons-testkit-testcontainers")
}
```

Reemplaza `VERSION` con la versión más reciente indicada en el [README](../../README.md).

### Maven

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.marcusprado02.commons</groupId>
      <artifactId>commons-bom</artifactId>
      <version>VERSION</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.marcusprado02.commons</groupId>
    <artifactId>commons-kernel-result</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.marcusprado02.commons</groupId>
    <artifactId>commons-kernel-errors</artifactId>
  </dependency>
</dependencies>
```

---

## Primer ejemplo completo

Este ejemplo crea una búsqueda de usuario simple que combina `Result` para la propagación tipada de errores y `Problem` para respuestas de error estructuradas.

```kotlin
import com.marcusprado02.commons.kernel.errors.*
import com.marcusprado02.commons.kernel.result.*

// Modelo de dominio
data class UserId(val value: String)
data class User(val id: UserId, val email: String)

// El repositorio retorna Result en vez de lanzar excepciones
interface UserRepository {
    fun findById(id: UserId): Result<User>
}

// Capa de servicio — los errores fluyen como valores, sin try/catch
class UserService(private val repo: UserRepository) {
    fun getUser(id: UserId): Result<User> =
        repo.findById(id)

    fun getUserEmail(id: UserId): Result<String> =
        getUser(id).map { it.email }
}

// Uso — fold en la frontera
fun handleRequest(id: String): String {
    val service = UserService(InMemoryUserRepository())
    return service.getUser(UserId(id)).fold(
        onFail = { problem -> "Error ${problem.category}: ${problem.message}" },
        onOk   = { user    -> "Found: ${user.email}" }
    )
}
```

`Result<T>` es `Ok<T>` (éxito) o `Fail` (lleva un `Problem`). `fold` te obliga a manejar ambos casos en el punto de llamada.

---

## Próximos pasos

- [kernel.md](./kernel.md) — Result, Problem, bloques de construcción DDD, utilidades de tiempo
- [ports.md](./ports.md) — contratos de port: caché, persistencia, mensajería, HTTP, correo electrónico
- [adapters.md](./adapters.md) — configuración de adapters: Redis, JPA, Kafka, OkHttp, SMTP
