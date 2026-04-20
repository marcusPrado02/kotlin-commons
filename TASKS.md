# TASKS.md — Melhorias para kotlin-commons

Lista de melhorias organizadas por categoria. Cada item é independente e pode ser
implementado em qualquer ordem.

`[x]` = implementado · `[ ]` = pendente

---

## Build & Infraestrutura

- [x] **T-01** Adicionar workflow de CI no GitHub Actions (build + test + detekt + ktlint em cada PR)
- [ ] **T-02** Configurar publicação no Maven Central com `maven-publish` + plugin de assinatura GPG
- [ ] **T-03** Integrar o plugin `org.jetbrains.kotlinx.binary-compatibility-validator`
- [x] **T-04** Habilitar o Gradle Build Cache (`org.gradle.caching=true`)
- [ ] **T-05** Adicionar `Renovate` (ou Dependabot) para atualização automática de dependências via PR
- [x] **T-06** Criar tarefa Gradle `checkAll` que agrupe lint + testes + compatibility-check em um único comando
- [ ] **T-07** Configurar `gradle/wrapper-validation-action` no CI para garantir integridade do `gradlew`
- [ ] **T-08** Separar `commons-bom` em artefato publicável independente com POM de tipo `pom`
- [ ] **T-09** Habilitar relatórios de cobertura com JaCoCo e publicar no Codecov
- [x] **T-10** Adicionar `settings.gradle.kts` com `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`

---

## commons-kernel-core

- [x] **T-11** Adicionar `Preconditions.requireNotBlank(value, lazyMessage)` _(já existia)_
- [x] **T-12** Adicionar `Strings.truncate(value: String, maxLength: Int, ellipsis: String = "…"): String` _(já existia)_
- [x] **T-13** Adicionar `Strings.toSlug()` _(já existia)_
- [x] **T-14** Adicionar `Collections.splitWhen(predicate)`
- [x] **T-15** Adicionar `Numbers.clamp(value, min, max)`
- [x] **T-16** Adicionar `Numbers.percentage(part: Number, total: Number): Double`
- [x] **T-17** Adicionar `Uuids.isValid(string: String): Boolean` _(já existia como `toUuidOrNull()`)_

---

## commons-kernel-errors

- [x] **T-18** Adicionar `StandardErrorCodes.CONFLICT`, `UNPROCESSABLE_ENTITY`, `TOO_MANY_REQUESTS` _(CONFLICT já existia)_
- [x] **T-19** Criar `ErrorCatalog` — interface marcadora para agrupar códigos de erro por domínio
- [x] **T-20** Adicionar `Problems.fromException(e: Throwable): Problem`
- [x] **T-21** Adicionar serialização JSON para `ProblemDetail` via `kotlinx.serialization` (sem depender de Jackson)
- [x] **T-22** Adicionar `Problem.withContext(key: String, value: Any): Problem`

---

## commons-kernel-result

- [x] **T-23** Adicionar `Either.swap(): Either<R, L>`
- [x] **T-24** Adicionar `Either.flatMapLeft(transform)`
- [x] **T-25** Adicionar `Result.zip(other: Result<R>): Result<Pair<T, R>>`
- [x] **T-26** Adicionar `Result.zipWith(other, transform)`
- [x] **T-27** Adicionar `Result.toEither(): Either<Problem, T>` e `Either<Problem, T>.toResult(): Result<T>`
- [x] **T-28** Adicionar `Option.orElse(other: Option<T>): Option<T>`
- [x] **T-29** Adicionar `Option.toResult(problem: Problem): Result<T>`
- [x] **T-30** Adicionar `Result.sequence(results: List<Result<T>>): Result<List<T>>`

---

## commons-kernel-ddd

- [x] **T-31** Adicionar `Command` e `CommandHandler<C : Command, R>`
- [x] **T-32** Adicionar `Query<R>` e `QueryHandler<Q : Query<R>, R>`
- [x] **T-33** Adicionar `DomainService` — interface marcadora
- [x] **T-34** Adicionar `Policy<E : DomainEvent>`
- [x] **T-35** Adicionar `Saga` — interface para processos de longa duração com compensação
- [ ] **T-36** Adicionar `ValueObject.validate(): Result<Unit>`
- [x] **T-37** Adicionar `AggregateRoot.clearEvents(): List<DomainEvent>` _(já existia como `pullDomainEvents()`)_
- [x] **T-38** Adicionar `EntityVersion.next(): EntityVersion` _(já existia como `increment()`)_
- [ ] **T-39** Adicionar `AuditTrail.markDeleted(actor, clock)` retornando novo `AuditTrail` com `DeletionStamp` preenchido
- [x] **T-40** Adicionar suporte a `TenantId` em `AggregateRoot` _(já existia)_

---

## commons-kernel-time

- [x] **T-41** Adicionar `TimeWindow.overlaps(other: TimeWindow): Boolean` _(já existia)_
- [x] **T-42** Adicionar `TimeWindow.contains(instant: Instant): Boolean` _(já existia)_
- [x] **T-43** Adicionar `TimeWindow.duration(): Duration` _(já existia)_
- [x] **T-44** Adicionar `TimeWindow.merge(other: TimeWindow): TimeWindow`
- [x] **T-45** Adicionar `FixedClockProvider.advance(duration: Duration): FixedClockProvider`

---

## commons-ports-persistence

- [x] **T-46** Adicionar `Repository.existsById(id: ID): Boolean` _(já existia)_
- [x] **T-47** Adicionar `Repository.count(): Long` _(já existia em `PageableRepository`)_
- [x] **T-48** Adicionar `Repository.saveAll(entities: Collection<T>): List<T>`
- [x] **T-49** Adicionar `Sort` com `SortField(field, direction)` em `PageRequest` _(já existia)_
- [x] **T-50** Adicionar `ProjectionRepository<ID, P>`

---

## commons-ports-messaging

- [x] **T-51** Adicionar `MessagePublisherPort.publishBatch(envelopes)` _(já existia)_
- [x] **T-52** Adicionar `MessageConsumerPort.poll(topic, group, maxCount): List<MessageEnvelope<ByteArray>>`
- [x] **T-53** Adicionar `DeadLetterPort`
- [x] **T-54** Adicionar `MessageEnvelope.withHeader(key, value): MessageEnvelope<T>`

---

## commons-ports-http

- [x] **T-55** Adicionar `HttpRequest.withHeader(name, value): HttpRequest` _(timeout já existia; headers no construtor)_
- [x] **T-56** Adicionar `HttpRequest.timeout: Duration?` _(já existia)_
- [x] **T-57** Adicionar `HttpBody.Json<T>(value: T, serializer: KSerializer<T>)` com `kotlinx.serialization`
- [x] **T-58** Adicionar extensões de conveniência: `get(uri)`, `post(uri, body)`, `put(uri, body)`, `delete(uri)`

---

## commons-ports-cache

- [x] **T-59** Adicionar `CachePort.getOrPut(key, ttl, loader)`
- [x] **T-60** Adicionar `CachePort.getAll(keys, type)`
- [x] **T-61** Adicionar `CachePort.invalidateByPrefix(prefix)`

---

## commons-ports-email

- [x] **T-62** Adicionar `EmailContent.withBoth(plain, html)`
- [x] **T-63** Adicionar `Email.withHeader(name, value)`
- [ ] **T-64** Adicionar `EmailPort.sendTemplate(templateId, context, to)` com Thymeleaf/Freemarker

---

## commons-adapters-persistence-jpa

- [x] **T-65** Implementar `JpaRepositoryAdapter.saveAll(entities)` com `CrudRepository.saveAll`
- [x] **T-66** Adicionar suporte a optimistic locking: detectar `ObjectOptimisticLockingFailureException`
- [x] **T-67** Adicionar `JpaRepositoryAdapter.existsById(id)` _(já existia)_

---

## commons-adapters-cache-redis

- [x] **T-68** Expor configuração de pool de conexão via construtor ou builder
- [x] **T-69** Adicionar suporte a Redis Cluster com `RedisClusterClient` do Lettuce
- [x] **T-70** Adicionar logging estruturado de cache hits/misses com SLF4J
- [x] **T-71** Permitir escolha de serializer via estratégia injetada no construtor

---

## commons-adapters-messaging-kafka

- [x] **T-72** `KafkaMessagePublisherAdapter.publishBatch` _(já existia com coroutines)_
- [x] **T-73** Adicionar propagação de `CorrelationId` como header Kafka no publisher e extração no consumer
- [x] **T-74** Adicionar dead-letter routing no consumer após N nacks consecutivos
- [x] **T-75** Expor `KafkaMessageConsumerAdapter.close()`
- [x] **T-76** Adicionar política de retry configurável (backoff exponencial) entre `nack` e próximo `receive`

---

## commons-adapters-http-okhttp

- [x] **T-77** Suporte a `HttpBody.Multipart` _(já existia no port e no adapter via `RequestConverters`)_
- [x] **T-78** Adicionar `LoggingInterceptor`
- [ ] **T-79** Integrar Resilience4j `CircuitBreaker` como interceptor OkHttp opcional
- [x] **T-80** Adicionar `RetryInterceptor` com backoff configurável
- [x] **T-81** Expor configuração de `ConnectionPool` e timeouts via builder dedicado

---

## commons-adapters-email-smtp

- [x] **T-82** Adicionar suporte a `multipart/alternative` (HTML + plain text no mesmo e-mail)
- [x] **T-83** Implementar `SmtpEmailAdapter.sendBatch` paralelo com `coroutineScope + async`
- [x] **T-84** Adicionar `SmtpSessionBuilder`

---

## commons-testkit-testcontainers

- [x] **T-85** Adicionar `MongoContainers`
- [x] **T-86** Adicionar `MySqlContainers`
- [x] **T-87** Adicionar `LocalStackContainers` para emular serviços AWS (S3, SQS, SNS)
- [x] **T-88** Adicionar `WireMockContainers`
- [x] **T-89** Adicionar `KafkaContainers.schemaRegistry`

---

## Documentação & Developer Experience

- [ ] **T-90** Adicionar KDoc a todas as classes e funções de API pública
- [ ] **T-91** Configurar `dokka` para gerar site HTML e publicar no GitHub Pages
- [x] **T-92** Adicionar badges no `README.md`: build status, cobertura, versão Maven Central
- [x] **T-93** Criar `CHANGELOG.md` seguindo o formato [Keep a Changelog](https://keepachangelog.com)
- [x] **T-94** Criar `CONTRIBUTING.md` com guia de setup local e convenções de commit
- [x] **T-95** Adicionar exemplos de uso no `README.md` para cada módulo
