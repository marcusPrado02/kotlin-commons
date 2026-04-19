# TASKS.md — Melhorias para kotlin-commons

Lista de melhorias organizadas por categoria. Cada item é independente e pode ser
implementado em qualquer ordem.

---

## Build & Infraestrutura

- [ ] **T-01** Adicionar workflow de CI no GitHub Actions (build + test + detekt + ktlint em cada PR)
- [ ] **T-02** Configurar publicação no Maven Central com `maven-publish` + plugin de assinatura GPG
- [ ] **T-03** Integrar o plugin `org.jetbrains.kotlinx.binary-compatibility-validator` para gerar e verificar dumps de API pública, impedindo regressões de ABI em releases
- [ ] **T-04** Habilitar o Gradle Build Cache (`org.gradle.caching=true`) e publicar cache remoto para acelerar builds em CI
- [ ] **T-05** Adicionar `Renovate` (ou Dependabot) para atualização automática de dependências via PR
- [ ] **T-06** Criar tarefa Gradle `checkAll` que agrupe lint + testes + compatibility-check em um único comando
- [ ] **T-07** Configurar `gradle/wrapper-validation-action` no CI para garantir integridade do `gradlew`
- [ ] **T-08** Separar `commons-bom` em artefato publicável independente com POM de tipo `pom`
- [ ] **T-09** Habilitar relatórios de cobertura com JaCoCo e publicar no Codecov
- [ ] **T-10** Adicionar `settings.gradle.kts` com `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` para substituir strings de projeto por acessores tipados

---

## commons-kernel-core

- [ ] **T-11** Adicionar `Preconditions.requireNotBlank(value, lazyMessage)` — atualmente só há `requireNotEmpty`; strings com apenas espaços passam sem erro
- [ ] **T-12** Adicionar `Strings.truncate(value: String, maxLength: Int, ellipsis: String = "…"): String`
- [ ] **T-13** Adicionar `Strings.toSlug()` — lowercase + remoção de acentos + substituição de espaços por hífen; útil em slugs de URL
- [ ] **T-14** Adicionar `Collections.splitWhen(predicate)` — divide uma lista em sublistas a cada vez que o predicado é verdadeiro
- [ ] **T-15** Adicionar `Numbers.clamp(value, min, max)` para limitar um número a um intervalo
- [ ] **T-16** Adicionar `Numbers.percentage(part: Number, total: Number): Double` com proteção contra divisão por zero
- [ ] **T-17** Adicionar `Uuids.isValid(string: String): Boolean` para validar formato de UUID sem lançar exceção

---

## commons-kernel-errors

- [ ] **T-18** Adicionar `StandardErrorCodes.CONFLICT`, `UNPROCESSABLE_ENTITY`, `TOO_MANY_REQUESTS` ao catálogo existente
- [ ] **T-19** Criar `ErrorCatalog` — interface para agrupar códigos de erro por domínio, facilitando documentação e rastreamento
- [ ] **T-20** Adicionar `Problems.fromException(e: Throwable): Problem` — converte exceções comuns (`IllegalArgumentException`, `NoSuchElementException` etc.) em `Problem` sem código boilerplate
- [ ] **T-21** Adicionar serialização JSON para `ProblemDetail` via `kotlinx.serialization` (sem depender de Jackson)
- [ ] **T-22** Adicionar `Problem.withContext(key: String, value: Any): Problem` para enriquecer um problema com metadados adicionais de forma imutável

---

## commons-kernel-result

- [ ] **T-23** Adicionar `Either.swap(): Either<R, L>` — inverte Left e Right; útil ao compor pipelines que precisam inverter a perspectiva de erro/sucesso
- [ ] **T-24** Adicionar `Either.flatMapLeft(transform)` para encadear operações no lado esquerdo (análogo ao `flatMapRight` existente)
- [ ] **T-25** Adicionar `Result.zip(other: Result<R>): Result<Pair<T, R>>` — combina dois resultados, propagando o primeiro erro encontrado
- [ ] **T-26** Adicionar `Result.zipWith(other, transform)` — combina dois resultados com transformação, para evitar `zip` + `map`
- [ ] **T-27** Adicionar `Result.toEither(): Either<Problem, T>` e `Either<Problem, T>.toResult(): Result<T>` para interoperabilidade
- [ ] **T-28** Adicionar `Option.orElse(other: Option<T>): Option<T>` — retorna `this` se `Some`, caso contrário retorna `other`
- [ ] **T-29** Adicionar `Option.toResult(problem: Problem): Result<T>` — converte `None` em `Fail(problem)` sem boilerplate
- [ ] **T-30** Adicionar `Result.sequence(results: List<Result<T>>): Result<List<T>>` — combina uma lista de resultados num único resultado com lista; falha se qualquer elemento for `Fail`

---

## commons-kernel-ddd

- [ ] **T-31** Adicionar `Command` e `CommandHandler<C : Command, R>` — abstrações para CQRS no lado de escrita
- [ ] **T-32** Adicionar `Query<R>` e `QueryHandler<Q : Query<R>, R>` — abstrações para CQRS no lado de leitura
- [ ] **T-33** Adicionar `DomainService` — interface marcadora para serviços de domínio; melhora legibilidade e facilita varredura estática
- [ ] **T-34** Adicionar `Policy<E : DomainEvent>` — abstração para reações a eventos de domínio (padrão Reaction/Policy do DDD)
- [ ] **T-35** Adicionar `Saga` — interface para processos de longa duração com compensação; inclui `step()` e `compensate()`
- [ ] **T-36** Adicionar `ValueObject.validate(): Result<Unit>` — permite que value objects declarem suas invariantes e retornem `Result` em vez de lançar exceções
- [ ] **T-37** Adicionar `AggregateRoot.clearEvents(): List<DomainEvent>` — padrão de dequeue de eventos; evita que o consumidor precise inspecionar o estado interno
- [ ] **T-38** Adicionar `EntityVersion.next(): EntityVersion` para modelar incremento de versão otimista sem expor implementação
- [ ] **T-39** Adicionar `AuditTrail.markDeleted(actor, clock)` retornando novo `AuditTrail` com `DeletionStamp` preenchido (abordagem imutável)
- [ ] **T-40** Adicionar suporte a `TenantId` em `AggregateRoot` como campo opcional para multi-tenancy

---

## commons-kernel-time

- [ ] **T-41** Adicionar `TimeWindow.overlaps(other: TimeWindow): Boolean`
- [ ] **T-42** Adicionar `TimeWindow.contains(instant: Instant): Boolean`
- [ ] **T-43** Adicionar `TimeWindow.duration(): Duration` como propriedade computada
- [ ] **T-44** Adicionar `TimeWindow.merge(other: TimeWindow): TimeWindow` — mínimo dos inícios e máximo dos fins
- [ ] **T-45** Adicionar `FixedClockProvider.advance(duration: Duration): FixedClockProvider` para testes que precisam simular passagem de tempo

---

## commons-ports-persistence

- [ ] **T-46** Adicionar `Repository.existsById(id: ID): Boolean`
- [ ] **T-47** Adicionar `Repository.count(): Long`
- [ ] **T-48** Adicionar `Repository.saveAll(entities: Collection<T>): List<T>` para inserção em lote
- [ ] **T-49** Adicionar `Sort` value class com lista de `SortOrder(field, direction)` e integrar em `PageRequest`
- [ ] **T-50** Adicionar `ProjectionRepository<ID, T, P>` — porta de leitura que retorna projeções `P` em vez da entidade completa `T`

---

## commons-ports-messaging

- [ ] **T-51** Adicionar `MessagePublisherPort.publishAll(envelopes: List<MessageEnvelope<*>>)` para publish em lote
- [ ] **T-52** Adicionar `MessageConsumerPort.poll(topic, group, maxCount: Int): List<MessageEnvelope<ByteArray>>` para consumo em lote
- [ ] **T-53** Adicionar `DeadLetterPort` — porta para roteamento de mensagens que falharam repetidamente
- [ ] **T-54** Adicionar `MessageEnvelope.withHeader(key: String, value: String): MessageEnvelope<T>` builder imutável

---

## commons-ports-http

- [ ] **T-55** Adicionar `HttpRequest.withHeader(name: String, value: String): HttpRequest` builder imutável
- [ ] **T-56** Adicionar `HttpRequest.timeout: Duration?` para configurar timeout por requisição
- [ ] **T-57** Adicionar `HttpBody.Json<T>(value: T, serializer: KSerializer<T>)` — serialização automática com `kotlinx.serialization` sem conversão manual para bytes
- [ ] **T-58** Adicionar extensões de conveniência em `HttpClientPort`: `get(uri)`, `post(uri, body)`, `put(uri, body)`, `delete(uri)`

---

## commons-ports-cache

- [ ] **T-59** Adicionar `CachePort.getOrPut(key, ttl, loader: suspend () -> T): T` — elimina o padrão get-then-put repetido em todo consumidor
- [ ] **T-60** Adicionar `CachePort.getAll(keys: Set<CacheKey>, type: Class<T>): Map<CacheKey, T>` para multi-get
- [ ] **T-61** Adicionar `CachePort.invalidateByPrefix(prefix: String)` para invalidação em massa por padrão de chave

---

## commons-ports-email

- [ ] **T-62** Adicionar `EmailContent.withBoth(plain: String, html: String): EmailContent` — factory para multipart/alternative; atualmente a lógica de HTML-ou-plain é implícita
- [ ] **T-63** Adicionar `Email.withHeader(name: String, value: String): Email` para cabeçalhos customizados (ex.: `X-Mailer`, `List-Unsubscribe`)
- [ ] **T-64** Adicionar `EmailPort.sendTemplate(templateId: String, context: Map<String, Any>, to: EmailAddress)` como método default que delega a implementações concretas com Thymeleaf/Freemarker

---

## commons-adapters-persistence-jpa

- [ ] **T-65** Implementar `JpaRepositoryAdapter.saveAll(entities)` usando `CrudRepository.saveAll` para aproveitar batching do Hibernate
- [ ] **T-66** Adicionar suporte a otimistic locking: detectar `ObjectOptimisticLockingFailureException` e convertê-la em `PersistenceException` com código padronizado
- [ ] **T-67** Adicionar `JpaRepositoryAdapter.existsById(id)` mapeado para `CrudRepository.existsById`

---

## commons-adapters-cache-redis

- [ ] **T-68** Expor configuração de pool de conexão (tamanho mínimo/máximo de conexões) via construtor ou builder
- [ ] **T-69** Adicionar suporte a Redis Cluster com `RedisClusterClient` do Lettuce
- [ ] **T-70** Adicionar `RedisCacheAdapter` com logging estruturado de cache hits/misses usando `org.slf4j.Logger`
- [ ] **T-71** Permitir escolha de serializer (JSON via kotlinx ou Java serialization) via estratégia injetada no construtor

---

## commons-adapters-messaging-kafka

- [ ] **T-72** Implementar `KafkaMessagePublisherAdapter.publishAll` com `producer.send` em loop + `producer.flush()` ao final para garantir entrega em lote
- [ ] **T-73** Adicionar propagação de `CorrelationId` como header Kafka no publisher e extração no consumer
- [ ] **T-74** Adicionar dead-letter routing no consumer: após N nacks consecutivos para o mesmo `messageId`, encaminhar para tópico `<topic>.dlq`
- [ ] **T-75** Expor `KafkaMessageConsumerAdapter.close()` que chama `consumer.unsubscribe()` e `consumer.close()` com timeout configurável
- [ ] **T-76** Adicionar política de retry configurável (backoff exponencial) entre `nack` e próximo `receive`

---

## commons-adapters-http-okhttp

- [ ] **T-77** Adicionar suporte a `HttpBody.Multipart` — envio de `multipart/form-data` com partes nomeadas (arquivos + campos)
- [ ] **T-78** Adicionar interceptor de logging configurável que registra URL, método, status e duração de cada requisição
- [ ] **T-79** Integrar Resilience4j `CircuitBreaker` como interceptor OkHttp opcional
- [ ] **T-80** Adicionar interceptor de retry com backoff exponencial configurável (número de tentativas, delay inicial, jitter)
- [ ] **T-81** Expor configuração de `ConnectionPool` e timeouts (connect, read, write) via builder dedicado

---

## commons-adapters-email-smtp

- [ ] **T-82** Adicionar suporte a `multipart/alternative` (HTML + plain text no mesmo e-mail) em vez de escolher um ou outro
- [ ] **T-83** Implementar `SmtpEmailAdapter.sendBatch` com `coroutineScope { emails.map { async { send(it) } }.awaitAll() }` para envio paralelo
- [ ] **T-84** Adicionar `SmtpSessionBuilder` — helper para construir `Session` com autenticação, TLS e timeouts sem exigir que o consumidor conheça `Properties`

---

## commons-testkit-testcontainers

- [ ] **T-85** Adicionar `MongoContainers` — factory para MongoDB com `mongo:7` e helper para obter connection string
- [ ] **T-86** Adicionar `MySqlContainers` factory similar a `PostgresContainers`
- [ ] **T-87** Adicionar `LocalStackContainers` para emular serviços AWS (S3, SQS, SNS) em testes de integração
- [ ] **T-88** Adicionar `WireMockContainers` para stubbing de HTTP externo em testes de integração
- [ ] **T-89** Adicionar `KafkaContainers.withSchemaRegistry()` — inicia Confluent Schema Registry junto com o broker Kafka

---

## Documentação & Developer Experience

- [ ] **T-90** Adicionar KDoc a todas as classes e funções de API pública (atualmente a maioria não tem documentação)
- [ ] **T-91** Configurar `dokka` para gerar site HTML de documentação e publicar no GitHub Pages
- [ ] **T-92** Adicionar badges no `README.md`: build status, cobertura, versão Maven Central
- [ ] **T-93** Criar `CHANGELOG.md` seguindo o formato [Keep a Changelog](https://keepachangelog.com) com versionamento semântico
- [ ] **T-94** Criar `CONTRIBUTING.md` com guia de setup local, convenções de commit e checklist de PR
- [ ] **T-95** Adicionar exemplos de uso no `README.md` para cada módulo (kernel, ports, adapters)
