# Contribuindo

Obrigado por contribuir com o kotlin-commons.

---

## Configuração local

```bash
git clone https://github.com/marcusPrado02/kotlin-commons.git
cd kotlin-commons

# Requer: JDK 21+, Docker (para testes de integração)
./gradlew build
```

---

## Executando verificações

```bash
./gradlew checkAll          # todas as verificações: compilar + lint + detekt + test + kover
./gradlew ktlintCheck       # estilo de código Kotlin
./gradlew detekt            # análise estática
./gradlew test              # testes unitários e de integração (inicia Testcontainers)
./gradlew koverVerify       # limiares de cobertura: 60% linha / 55% branch
./gradlew koverHtmlReport   # relatório HTML em build/reports/kover/html/
```

**O CI executa `checkAll` a cada push.** PRs devem passar no CI antes do merge.

---

## Estrutura dos módulos

```
commons-bom/                        Bill of Materials
commons-kernel-*/                   Kotlin puro, sem deps de framework
commons-ports-*/                    Apenas contratos de interface
commons-adapters-*/                 Implementações (dependem de um port + biblioteca)
commons-testkit-testcontainers/     Helper de teste — singletons Testcontainers
```

### Adicionando um novo port

1. Criar diretório `commons-ports-<nome>/`.
2. Adicionar ao `settings.gradle.kts` com `include(...)`.
3. Criar `build.gradle.kts` com `plugins { id("kotlin-commons") }`.
4. Criar a interface em `src/main/kotlin/com/marcusprado02/commons/ports/<nome>/`.
5. Aplicar `explicitApi()` — todas as declarações públicas precisam de modificadores de visibilidade explícitos.
6. Adicionar `api(project(":commons-ports-<nome>"))` às restrições do `commons-bom/build.gradle.kts`.
7. Escrever testes em `src/test/kotlin/` com Kotest `FunSpec`.

### Adicionando um novo adapter

1. Seguir os mesmos passos do port, usando a nomenclatura `commons-adapters-<tecnologia>-<port>`.
2. No `build.gradle.kts`, adicionar `implementation(project(":commons-ports-<nome>"))` mais a dependência da biblioteca.
3. Implementar a interface do port e adicionar testes de integração com `commons-testkit-testcontainers`.
4. Desative o Kover somente se o módulo não tiver fontes de teste (`kover { disable() }`) — adapters devem ter testes de integração.

---

## Convenções de teste

- Estilo: **Kotest `FunSpec`** — `test("descrição") { ... }`.
- Testes de integração usam **Testcontainers** via singletons de `commons-testkit-testcontainers`.
- Limiares de cobertura: **60% linha / 55% branch** por módulo. Verifique com `./gradlew koverVerify`.
- Todos os módulos usam **`explicitApi()`**.

---

## Convenções de commit

kotlin-commons usa [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(kernel-result): adicionar operador zipWith para combinar dois Results
fix(adapters-kafka): tratar cabeçalho correlationId nulo
docs(pt): atualizar snippet de instalação no getting-started
test(kernel-errors): cobrir branches de cause nullable
chore: atualizar testcontainers para 1.20.0
```

O escopo é o nome do módulo sem o prefixo `commons-`.

---

## Processo de release

Releases são acionadas por uma tag Git:

```bash
git tag v1.2.3
git push origin v1.2.3
```

O workflow de CI `publish` é acionado automaticamente, assina todos os artefatos e publica no Maven Central via Sonatype Central Portal.

---

## Convenção de tradução

**Inglês (`docs/en/`) é a única fonte de verdade.**

Ao atualizar documentação:
1. Atualizar `docs/en/<arquivo>.md` primeiro.
2. Atualizar `docs/pt/<arquivo>.md` e `docs/es/<arquivo>.md` no **mesmo pull request**.
3. A descrição do PR deve indicar quais seções foram alteradas para que os revisores possam verificar as traduções.

O que NÃO é traduzido: comentários KDoc no código-fonte, mensagens de erro e strings de log, nomes de branches, commits e tags.
