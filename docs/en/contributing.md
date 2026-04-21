# Contributing

Thank you for contributing to kotlin-commons.

---

## Local setup

```bash
git clone https://github.com/marcusPrado02/kotlin-commons.git
cd kotlin-commons

# Requires: JDK 21+, Docker (for integration tests)
./gradlew build
```

---

## Running checks

```bash
./gradlew checkAll          # all checks: compile + lint + detekt + test + kover
./gradlew ktlintCheck       # Kotlin code style
./gradlew detekt            # static analysis
./gradlew test              # unit + integration tests (starts Testcontainers)
./gradlew koverVerify       # coverage thresholds: 60% line / 55% branch
./gradlew koverHtmlReport   # HTML coverage report at build/reports/kover/html/
```

**CI runs `checkAll` on every push.** PRs must pass CI before merge.

---

## Module structure

```
commons-bom/                        Bill of Materials
commons-kernel-*/                   Pure Kotlin, no framework deps
commons-ports-*/                    Interface contracts only
commons-adapters-*/                 Implementations (depend on a port + a library)
commons-testkit-testcontainers/     Test helper — Testcontainers singletons
```

### Adding a new port

1. Create `commons-ports-<name>/` module directory.
2. Add `commons-ports-<name>` to `settings.gradle.kts` `include(...)`.
3. Create `commons-ports-<name>/build.gradle.kts`:
   ```kotlin
   plugins { id("kotlin-commons") }
   // no extra dependencies for pure interface modules
   ```
4. Create the interface in `src/main/kotlin/com/marcusprado02/commons/ports/<name>/`.
5. Apply `explicitApi()` — all public declarations need explicit visibility modifiers.
6. Add `api(project(":commons-ports-<name>"))` to `commons-bom/build.gradle.kts` constraints.
7. Write tests in `src/test/kotlin/` with Kotest `FunSpec` (mock the interface, test the contract).

### Adding a new adapter

1. Follow the same steps as a new port, using `commons-adapters-<technology>-<port>` naming.
2. In `build.gradle.kts`, add `implementation(project(":commons-ports-<name>"))` plus the library dependency.
3. Implement the port interface and add integration tests using `commons-testkit-testcontainers`.
4. Disable Kover only if the module has zero test sources (`kover { disable() }`) — adapters must have integration tests.

---

## Testing conventions

- Test style: **Kotest `FunSpec`** — `test("description") { ... }`.
- Integration tests use **Testcontainers** via `commons-testkit-testcontainers` singletons (containers are started once per Gradle test JVM).
- Coverage thresholds: **60% line / 55% branch** per module. Check with `./gradlew koverVerify`.
- All modules use **`explicitApi()`** — public declarations need explicit `public` or `internal` visibility.

---

## Commit conventions

kotlin-commons uses [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(kernel-result): add zipWith operator for combining two Results
fix(adapters-kafka): handle null correlationId header gracefully
docs(en): update getting-started installation snippet
test(kernel-errors): cover nullable cause branches
chore: bump testcontainers to 1.20.0
```

Scope is the module name without the `commons-` prefix (e.g. `kernel-result`, `ports-cache`, `adapters-kafka`).

---

## Release process

Releases are triggered by a Git tag. No manual publish step needed.

```bash
git tag v1.2.3
git push origin v1.2.3
```

The CI `publish` workflow triggers automatically, signs all artifacts, and publishes to Maven Central via the Sonatype Central Portal.

---

## Translation convention

**English (`docs/en/`) is the single source of truth.**

When updating documentation:
1. Update `docs/en/<file>.md` first.
2. Update `docs/pt/<file>.md` and `docs/es/<file>.md` in the **same pull request**.
3. The PR description must note which sections were changed, so reviewers can verify translations.

What is NOT translated:
- KDoc comments in source code
- Error messages and log strings
- Branch names, commit messages, and tags
