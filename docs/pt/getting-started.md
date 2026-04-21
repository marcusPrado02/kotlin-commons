# Primeiros Passos

kotlin-commons é um conjunto modular de bibliotecas Kotlin para serviços backend JVM em produção. Está organizado em três camadas: **kernel** (primitivos de domínio puro sem dependências de framework), **ports** (contratos de interface para infraestrutura) e **adapters** (implementações baseadas em bibliotecas populares).

A biblioteca existe porque preocupações transversais comuns — tratamento tipado de erros, blocos de construção DDD e abstrações de infraestrutura — não devem ser reinventadas em cada serviço. kotlin-commons fornece isso como módulos pequenos e focados que podem ser adotados individualmente ou em conjunto.

---

## Pré-requisitos

- JVM 21+
- Kotlin 2.1.0+
- Gradle 8+ ou Maven 3.9+

---

## Instalação

### Gradle (recomendado — use o BOM)

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))

    // escolha apenas os módulos necessários
    implementation("io.github.marcusprado02.commons:commons-kernel-result")
    implementation("io.github.marcusprado02.commons:commons-kernel-errors")
    implementation("io.github.marcusprado02.commons:commons-ports-cache")
    implementation("io.github.marcusprado02.commons:commons-adapters-cache-redis")

    // helpers de teste
    testImplementation("io.github.marcusprado02.commons:commons-testkit-testcontainers")
}
```

Substitua `VERSION` pela versão mais recente indicada no [README](../../README.md).

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

## Primeiro exemplo completo

Este exemplo cria uma busca de usuário simples que combina `Result` para propagação tipada de erros e `Problem` para respostas de erro estruturadas.

```kotlin
import com.marcusprado02.commons.kernel.errors.*
import com.marcusprado02.commons.kernel.result.*

// Modelo de domínio
data class UserId(val value: String)
data class User(val id: UserId, val email: String)

// Repositório retorna Result em vez de lançar exceção
interface UserRepository {
    fun findById(id: UserId): Result<User>
}

// Camada de serviço — erros fluem como valores, sem try/catch
class UserService(private val repo: UserRepository) {
    fun getUser(id: UserId): Result<User> = repo.findById(id)

    fun getUserEmail(id: UserId): Result<String> =
        getUser(id).map { it.email }
}

// Uso — fold na fronteira
fun handleRequest(id: String): String {
    val service = UserService(InMemoryUserRepository())
    return service.getUser(UserId(id)).fold(
        onFail = { problem -> "Erro ${problem.category}: ${problem.message}" },
        onOk   = { user    -> "Encontrado: ${user.email}" }
    )
}
```

`Result<T>` é `Ok<T>` (sucesso) ou `Fail` (carrega um `Problem`). `fold` força o tratamento de ambos os casos no ponto de chamada.

---

## Próximos passos

- [kernel.md](kernel.md) — Result, Problem, blocos DDD, utilitários de tempo
- [ports.md](ports.md) — contratos de port: cache, persistência, mensageria, HTTP, e-mail
- [adapters.md](adapters.md) — configuração de adapters: Redis, JPA, Kafka, OkHttp, SMTP
