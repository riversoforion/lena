# Kotlin Multiplatform Migration Plan

Index of phase docs. Each phase doc has a status header.

| Phase | Doc | Status |
| --- | --- | --- |
| 1 | [Build system foundation](phase-1-build-foundation.md) | Done |
| 2 | [Port lena-config core to commonMain](phase-2-core-port.md) | Done |
| 3 | [Idiomatic Kotlin API redesign](phase-3-idiomatic-api.md) | Done |
| 4 | [Tests, examples, scaffolding](phase-4-tests-examples.md) | Done |
| 5 | [KSP annotation processor foundation](phase-5-ksp-processor.md) | Deferred (scaffold only) |
| 6 | [Type coercion & complex types](phase-6-type-coercion.md) | In Progress |
| 7 | [Validation & observability](phase-7-validation-observability.md) | Planned |
| — | [Deprioritized: future explorations](deprioritized.md) | Deprioritized |
| — | [Strategic positioning (post-Phase 7)](positioning.md) | Reference |

## Context

Lena is an early-stage Java 21 configuration library (`lena-config`) plus a half-started
Java annotation-processor experiment. The owner wants to convert it to **Kotlin**, usable
from both **Java (JVM)** and **Kotlin/Native**, on the **latest Kotlin (2.4.0)** and
**latest Gradle (9.x)** with Kotlin-DSL build scripts. This is a learning/experimentation
project: backward compatibility is explicitly a non-goal, and the build is allowed to be
broken between steps.

Confirmed decisions:

- **Native targets:** Desktop tier — `jvm`, `macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64` (`macosX64` deprecated, replaced by `linuxArm64`).
- **Processors:** Deferred through Phase 4, delivered in Phase 5.
- **API style:** Idiomatic Kotlin — property delegates, drop the global singleton registry,
  keep `@JvmStatic`/`@JvmOverloads` facades so Java callers stay ergonomic.

Original stack (replaced in Phase 1): Gradle 8.10.2, no Kotlin, three Java convention plugins in
`buildSrc`, version catalogs generated from BOMs via `dev.aga.gradle.version-catalog-generator`.
The old Java APT processor used `io.toolisticon.aptk` to generate wrappers — all of it was
removed/parked.

## Goals & non-goals

- **Goal:** A KMP `lena-config` whose core lives in `commonMain` and compiles for JVM + the four
  native desktop targets, with a clean idiomatic Kotlin API and good Java interop on the JVM.
- **Goal:** Evaluate the existing logic for correctness while porting.
- **Goal:** Latest Gradle + Kotlin, Kotlin-DSL convention plugins rewired for KMP.
- **Non-goal (now):** Keeping intermediate states building; the empty placeholder modules
  (`lena-config-consul`, `-etcd`, `-sql`, etc.).
