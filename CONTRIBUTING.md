# Contributing to kotlin-commons

Thank you for your interest in contributing!

## Prerequisites

- JDK 21 (Temurin recommended)
- Docker (required for Testcontainers integration tests)

## Local Setup

```bash
git clone https://github.com/marcusprado02/kotlin-commons.git
cd kotlin-commons
./gradlew build
```

## Running Checks

```bash
# All checks (lint + tests)
./gradlew checkAll

# Individual
./gradlew ktlintCheck
./gradlew detekt
./gradlew test
```

## Code Style

- Kotlin formatting is enforced via **ktlint**. Run `./gradlew ktlintFormat` to auto-fix.
- Static analysis uses **detekt** with the project's `detekt.yml`.
- All public declarations must have explicit `public` modifier (`explicitApi()` is enabled).
- Treat all warnings as errors (`allWarningsAsErrors = true`).

## Commit Conventions

Use conventional commits:
```
feat(module): short description
fix(module): short description
chore(build): short description
docs: short description
```

Examples:
- `feat(kernel-result): add Result.zip combinator`
- `fix(adapter-kafka): handle null message key in consumer`

## Pull Request Checklist

- [ ] Tests added for new behaviour
- [ ] `./gradlew checkAll` passes locally
- [ ] Commit message follows convention
- [ ] `CHANGELOG.md` updated under `[Unreleased]`

## Module Structure

| Module | Purpose |
|---|---|
| `commons-kernel-*` | Pure domain primitives, no infrastructure dependencies |
| `commons-ports-*` | Interface contracts for infrastructure (persistence, messaging, etc.) |
| `commons-adapters-*` | Concrete implementations of ports |
| `commons-testkit-*` | Test helpers and Testcontainer factories |
| `commons-bom` | Bill of Materials for unified version management |
