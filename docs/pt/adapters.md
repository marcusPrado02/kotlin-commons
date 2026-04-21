# Adapters

Módulos de adapter implementam interfaces de port usando bibliotecas específicas. Adicione a dependência de adapter apenas quando precisar da implementação concreta.

---

## adapters-cache-redis

**Implementa:** `CachePort` usando Spring Data Redis com serialização Jackson.

**Dependência:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-cache-redis")
```

**Configuração:**

```kotlin
@Configuration
class ConfigCache(private val redis: RedisTemplate<String, ByteArray>) {
    @Bean
    fun cachePort(): CachePort = RedisCacheAdapter(redis)

    @Bean
    fun cachePortCustom(redis: RedisTemplate<String, ByteArray>, mapper: ObjectMapper): CachePort =
        RedisCacheAdapter(redis, objectMapper = mapper)
}
```

**Quando considerar uma alternativa:** Use `RedisClusterCacheAdapter` para topologia Redis Cluster. Use um adapter diferente se precisar de scripting Redis, sorted sets ou pub/sub.

---

## adapters-persistence-jpa

**Implementa:** `Repository<E, I>` (via `JpaRepositoryAdapter`) e `PageableRepository<E, I>` (via `JpaPageableRepositoryAdapter`) usando Spring Data JPA. Ambas são classes abstratas — subclassifique-as para cada agregado.

**Dependência:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-persistence-jpa")
```

**Configuração:**

```kotlin
@Repository
class UsuarioJpaAdapter(jpa: JpaRepository<UsuarioEntity, String>) :
    JpaPageableRepositoryAdapter<UsuarioEntity, String>(jpa)
```

**Quando considerar uma alternativa:** JOOQ, queries nativas, multi-tenancy ou outro ORM (Exposed, MyBatis).

---

## adapters-messaging-kafka

**Implementa:** `MessagePublisherPort` e `MessageConsumerPort` usando o cliente Apache Kafka.

**Dependência:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-messaging-kafka")
```

**Configuração:**

```kotlin
@Bean
fun kafkaPublisher(): MessagePublisherPort {
    val producer = KafkaProducer<String, ByteArray>(mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java.name,
    ))
    return KafkaMessagePublisherAdapter(producer)
}

@Bean
fun kafkaConsumer(deadLetter: DeadLetterPort): MessageConsumerPort {
    val consumer = KafkaConsumer<String, ByteArray>(mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ConsumerConfig.GROUP_ID_CONFIG to "meu-servico-grupo",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java.name,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
    ))
    return KafkaMessageConsumerAdapter(
        consumer       = consumer,
        groupId        = "meu-servico-grupo",
        maxNacks       = 3,
        deadLetterPort = deadLetter,
    )
}
```

**Quando considerar uma alternativa:** Kafka Streams ou Spring Kafka para stream processing, agregações com janelas ou semântica exactly-once.

---

## adapters-http-okhttp

**Implementa:** `HttpClientPort` (`OkHttpClientAdapter`) usando OkHttp.

**Dependência:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-http-okhttp")
```

**Configuração:**

```kotlin
@Bean
fun httpClientPort(): HttpClientPort {
    val okhttp = OkHttpClientBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(30))
        .addInterceptor(LoggingInterceptor())
        .build()
    return OkHttpClientAdapter(okhttp)
}
```

**Quando considerar uma alternativa:** Use OkHttp diretamente para streaming, WebSockets ou chains de interceptor avançados.

---

## adapters-email-smtp

**Implementa:** `EmailPort` (`SmtpEmailAdapter`) usando Jakarta Mail.

**Dependência:**

```kotlin
implementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
implementation("io.github.marcusprado02.commons:commons-adapters-email-smtp")
```

**Configuração:**

```kotlin
@Bean
fun emailPort(): EmailPort {
    val session = SmtpSessionBuilder()
        .host("smtp.example.com")
        .port(587)
        .credentials("noreply@example.com", System.getenv("SMTP_PASSWORD"))
        .tls(true)
        .build()
    return SmtpEmailAdapter(session)
}
```

**Quando considerar uma alternativa:** Use SendGrid, AWS SES ou Mailgun para rastreamento de entrega, tratamento de bounce ou envio em massa.

---

## testkit-testcontainers

**O que fornece:** Instâncias singleton de Testcontainers pré-configuradas. Os containers iniciam uma vez por JVM de teste Gradle.

**Dependência:**

```kotlin
testImplementation(platform("io.github.marcusprado02.commons:commons-bom:VERSION"))
testImplementation("io.github.marcusprado02.commons:commons-testkit-testcontainers")
```

**Uso:**

```kotlin
import com.marcusprado02.commons.testkit.testcontainers.*

class TesteIntegracaoKafka : FunSpec({
    val bootstrap = KafkaContainers.instance.bootstrapServers

    test("pode publicar e receber uma mensagem") {
        // lógica do teste
    }
})
```

Containers disponíveis: `KafkaContainers`, `PostgresContainers`, `MongoContainers`, `MySqlContainers`, `RedisContainers`, `WireMockContainers`, `GreenMailContainers`, `LocalStackContainers`.
