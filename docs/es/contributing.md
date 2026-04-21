# Contribuyendo

Gracias por contribuir a kotlin-commons.

---

## Configuración local

```bash
git clone https://github.com/marcusPrado02/kotlin-commons.git
cd kotlin-commons

# Requiere: JDK 21+, Docker (para pruebas de integración)
./gradlew build
```

---

## Ejecutar verificaciones

```bash
./gradlew checkAll          # todas las verificaciones: compilar + lint + detekt + test + kover
./gradlew ktlintCheck       # estilo de código Kotlin
./gradlew detekt            # análisis estático
./gradlew test              # pruebas unitarias e de integración (inicia Testcontainers)
./gradlew koverVerify       # umbrales de cobertura: 60% línea / 55% branch
./gradlew koverHtmlReport   # reporte HTML de cobertura en build/reports/kover/html/
```

**El CI ejecuta `checkAll` en cada push.** Los PRs deben pasar el CI antes del merge.

---

## Estructura de módulos

```
commons-bom/                        Bill of Materials
commons-kernel-*/                   Kotlin puro, sin deps de framework
commons-ports-*/                    Solo contratos de interfaz
commons-adapters-*/                 Implementaciones (dependen de un port + biblioteca)
commons-testkit-testcontainers/     Helper de prueba — singletons Testcontainers
```

### Agregar un nuevo port

1. Crear el directorio `commons-ports-<nombre>/`.
2. Agregar `commons-ports-<nombre>` a `settings.gradle.kts` con `include(...)`.
3. Crear `commons-ports-<nombre>/build.gradle.kts`:
   ```kotlin
   plugins { id("kotlin-commons") }
   // sin dependencias adicionales para módulos de interfaz pura
   ```
4. Crear la interfaz en `src/main/kotlin/com/marcusprado02/commons/ports/<nombre>/`.
5. Aplicar `explicitApi()` — todas las declaraciones públicas necesitan modificadores de visibilidad explícitos.
6. Agregar `api(project(":commons-ports-<nombre>"))` a las restricciones de `commons-bom/build.gradle.kts`.
7. Escribir pruebas en `src/test/kotlin/` con Kotest `FunSpec` (simula la interfaz, prueba el contrato).

### Agregar un nuevo adapter

1. Seguir los mismos pasos que para un nuevo port, usando la nomenclatura `commons-adapters-<tecnología>-<port>`.
2. En `build.gradle.kts`, agregar `implementation(project(":commons-ports-<nombre>"))` más la dependencia de la biblioteca.
3. Implementar la interfaz del port y agregar pruebas de integración usando `commons-testkit-testcontainers`.
4. Desactiva Kover solo si el módulo no tiene fuentes de prueba (`kover { disable() }`) — los adapters deben tener pruebas de integración.

---

## Convenciones de prueba

- Estilo de prueba: **Kotest `FunSpec`** — `test("descripción") { ... }`.
- Las pruebas de integración usan **Testcontainers** vía singletons de `commons-testkit-testcontainers` (los contenedores se inician una vez por JVM de prueba de Gradle).
- Umbrales de cobertura: **60% línea / 55% branch** por módulo. Verifica con `./gradlew koverVerify`.
- Todos los módulos usan **`explicitApi()`** — las declaraciones públicas necesitan visibilidad `public` o `internal` explícita.

---

## Convenciones de commits

kotlin-commons usa [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(kernel-result): add zipWith operator for combining two Results
fix(adapters-kafka): handle null correlationId header gracefully
docs(es): update getting-started installation snippet
test(kernel-errors): cover nullable cause branches
chore: bump testcontainers to 1.20.0
```

El scope es el nombre del módulo sin el prefijo `commons-` (por ejemplo, `kernel-result`, `ports-cache`, `adapters-kafka`).

---

## Proceso de release

Los releases se disparan con una etiqueta Git. No se necesita un paso manual de publicación.

```bash
git tag v1.2.3
git push origin v1.2.3
```

El workflow de CI `publish` se dispara automáticamente, firma todos los artefactos y publica en Maven Central vía Sonatype Central Portal.

---

## Convención de traducción

**El inglés (`docs/en/`) es la única fuente de verdad.**

Al actualizar documentación:
1. Actualizar `docs/en/<archivo>.md` primero.
2. Actualizar `docs/pt/<archivo>.md` y `docs/es/<archivo>.md` en el **mismo pull request**.
3. La descripción del PR debe indicar qué secciones se modificaron, para que los revisores puedan verificar las traducciones.

Lo que NO se traduce:
- Comentarios KDoc en el código fuente
- Mensajes de error y cadenas de log
- Nombres de branches, mensajes de commit y etiquetas
