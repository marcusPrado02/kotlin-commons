# Multilingual Documentation Design

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add comprehensive, trilingual (EN / PT / ES) documentation to kotlin-commons, organised by architectural layer, living entirely in the repository as Markdown files.

**Architecture:** English is the master language. Portuguese and Spanish are translated from English at creation time and updated whenever the English master changes. Dokka-generated API docs remain English-only. No static site generator is introduced at this stage — GitHub's native Markdown rendering is sufficient.

**Tech Stack:** Markdown, existing Dokka (KDoc), GitHub Actions CI (no new tooling required)

---

## File Structure

```
README.md                        (rewritten: language selector + badges + quick example)
docs/
  en/
    getting-started.md
    kernel.md
    ports.md
    adapters.md
    contributing.md
  pt/
    getting-started.md
    kernel.md
    ports.md
    adapters.md
    contributing.md
  es/
    getting-started.md
    kernel.md
    ports.md
    adapters.md
    contributing.md
```

Total: 1 root README + 15 language-specific files = **16 files**.

---

## README.md (root)

Rewritten to serve as a language-neutral entry point:

- CI, Maven Central, Kotlin version, JVM, and Apache 2.0 licence badges
- 2–3 sentence description of the library in English (the README itself stays in EN per GitHub convention)
- Language selector: `🇺🇸 English` | `🇧🇷 Português` | `🇪🇸 Español` — each links to `docs/{lang}/getting-started.md`
- One concise code example per major layer (kernel, ports, adapters) to give readers an instant taste

---

## Content Specification per File

### `getting-started.md` (all three languages)

1. What kotlin-commons is and why it exists (2 paragraphs)
2. Prerequisites: JVM 21, Kotlin 2.1.0+
3. Adding to a project — Gradle (BOM + individual modules) and Maven snippets
4. First end-to-end example combining `Result` and `Problems`
5. Navigation links to `kernel.md`, `ports.md`, `adapters.md`

### `kernel.md` (all three languages)

One section per kernel module. Each section contains:

- **What it is and when to use it** — 1 paragraph
- **Key public types / functions** — annotated code examples for each
- **Design decision** — explicit reasoning (e.g. why `Result<E, A>` instead of `kotlin.Result`, why `Problem` is a data class with `@Serializable`, why `DomainException` is sealed)

Modules covered: `kernel-core`, `kernel-result`, `kernel-errors`, `kernel-ddd`, `kernel-time`

### `ports.md` (all three languages)

One section per port module. Each section contains:

- **The interface contract** — the key interface shown in full
- **Consumer-side usage example** — how application code uses the port (no implementation details)
- **When to use vs. alternatives** — explicit trade-off statement
- **Design decision** — why this is an interface and not a direct implementation (hexagonal architecture rationale)

Ports covered: `ports-cache`, `ports-persistence`, `ports-messaging`, `ports-http`, `ports-email`

### `adapters.md` (all three languages)

One section per adapter + testkit. Each section contains:

- **What it implements** — which port(s) and with which library
- **Minimum configuration** — dependency snippet + configuration class/properties
- **Complete usage example** — from wiring to first call
- **When to consider an alternative** — explicit guidance (e.g. use a different HTTP client if you need streaming)

Adapters covered: `adapters-cache-redis`, `adapters-persistence-jpa`, `adapters-messaging-kafka`, `adapters-http-okhttp`, `adapters-email-smtp`, `testkit-testcontainers`

### `contributing.md` (all three languages)

Expanded version of the current root `CONTRIBUTING.md`:

- Local setup (clone, JDK 21, Docker)
- Running checks (`checkAll`, `ktlintCheck`, `detekt`, `test`)
- Module structure — how to add a new port or adapter (step-by-step)
- Testing conventions: Kotest `FunSpec`, Testcontainers patterns, Kover thresholds (60% line / 55% branch), `explicitApi()` requirement
- Commit conventions (Conventional Commits)
- Release process: create `vX.Y.Z` tag → publish workflow triggers automatically
- Translation convention: **EN is the source of truth** — update `docs/en/` first, then update `docs/pt/` and `docs/es/` in the same PR

---

## Translation Workflow

| Step | Who |
|---|---|
| Write or update `docs/en/` | Author (EN master) |
| Translate to `docs/pt/` and `docs/es/` | Author, with AI assistance |
| Review translations | Repository owner |

**What is NOT translated:**
- KDoc comments in source code (remain in English)
- Error messages and log strings
- Branch names, commit messages, tags

**Adding a new language in the future:** create `docs/{lang}/` with the same 5 files. No structural changes required.

---

## Maintenance Convention

Documented in `docs/en/contributing.md` (and translations):

> When updating documentation, always update `docs/en/` first. The English files are the single source of truth. Update the Portuguese and Spanish translations in the same pull request.

The root `CONTRIBUTING.md` is replaced by `docs/{lang}/contributing.md`. A minimal stub remains at `CONTRIBUTING.md` redirecting readers to `docs/en/contributing.md` for backward compatibility with GitHub's contributing file detection.

---

## Out of Scope

- Static site generator (MkDocs, Docusaurus, etc.) — deferred until the project has more contributors
- GitHub Pages hosting — deferred; can be added later without restructuring
- Translating KDoc comments — not cost-effective; Dokka API docs remain English-only
- Automated translation checks in CI — not added; manual review is sufficient at this scale
