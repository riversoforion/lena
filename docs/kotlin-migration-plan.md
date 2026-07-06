# Plan: Overhaul Lena into a Kotlin Multiplatform configuration library

## Context

Lena is an early-stage Java 21 configuration library (`lena-config`) plus a half-started
Java annotation-processor experiment. The owner wants to convert it to **Kotlin**, usable
from both **Java (JVM)** and **Kotlin/Native**, on the **latest Kotlin (2.4.0)** and
**latest Gradle (9.x)** with Kotlin-DSL build scripts. This is a learning/experimentation
project: backward compatibility is explicitly a non-goal, and the build is allowed to be
broken between steps.

Confirmed decisions:

- **Native targets:** Desktop tier — `jvm`, `macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64` (`macosX64` deprecated, replaced by `linuxArm64`).
- **Processors:** Deferred. Scaffold a KSP module but do not implement it now.
- **API style:** Idiomatic Kotlin — property delegates, drop the global singleton registry,
  keep `@JvmStatic`/`@JvmOverloads` facades so Java callers stay ergonomic.

Current stack (to be replaced): Gradle 8.10.2, no Kotlin, three Java convention plugins in
`buildSrc`, version catalogs generated from BOMs via `dev.aga.gradle.version-catalog-generator`.
The old Java APT processor used `io.toolisticon.aptk` to generate wrappers — all of it will
be removed/parked.

## Goals & non-goals

- **Goal:** A KMP `lena-config` whose core lives in `commonMain` and compiles for JVM + the four
  native desktop targets, with a clean idiomatic Kotlin API and good Java interop on the JVM.
- **Goal:** Evaluate the existing logic for correctness while porting (see "Correctness review").
- **Goal:** Latest Gradle + Kotlin, Kotlin-DSL convention plugins rewired for KMP.
- **Non-goal (now):** Implementing either processor; keeping intermediate states building;
  the empty placeholder modules (`lena-config-consul`, `-etcd`, `-sql`, etc.).

---

## Phase 1 — Build system foundation

1. **Gradle wrapper → 9.x.** Update `gradle/wrapper/gradle-wrapper.properties`
   (`./gradlew wrapper --gradle-version=9.x`).
2. **Version catalog** (`gradle/libs.versions.toml`): add `kotlin = "2.4.0"`, `ksp` (matching
   Kotlin, e.g. `2.4.0-x.x.x`), and plugin aliases for `kotlin-multiplatform` and
   `com.google.devtools.ksp`. Keep JVM-only test libs (JUnit5/AssertJ/Mockito/system-stubs) but
   move shared multiplatform testing to `kotlin("test")`. Reconsider whether the BOM-generator
   plugin is still worth it; likely simplify to plain catalog entries.
3. **Rewrite `buildSrc` convention plugins** (`buildSrc/src/main/kotlin/`), replacing the three
   Java ones:
   - `lena.kmp-library.gradle.kts` — applies `org.jetbrains.kotlin.multiplatform`, declares the
     desktop-tier targets, sets `jvmToolchain(21)` with `jvmTarget = JVM_17` (≥17 requirement;
     compile on 21), `withSourcesJar`, `explicitApi()`, and the maven-publish/signing block
     ported from `lena.libraries.gradle.kts`.
   - `lena.kmp-conventions.gradle.kts` — shared compiler options (e.g. `-Xexpect-actual-classes`),
     common/native source-set wiring, `kotlin-test` on `commonTest`, JUnit5 on `jvmTest`.
   - Park `lena.examples.gradle.kts` until examples are reworked (Phase 4).
4. **`settings.gradle.kts`:** keep `lena-config`; drop the BOM `versionCatalogs { generate(...) }`
   blocks if the generator is removed; comment out example/placeholder includes until ported.

## Phase 2 — Port `lena-config` core to `commonMain`

Target package stays `org.riversoforion.lena.config`. Move logic into
`lena-config/src/commonMain/kotlin/...`, with `jvmMain`/`nativeMain` for platform pieces.
Key Java→Kotlin substitutions (the heart of the work):

| Java construct (current) | Kotlin/Common replacement |
| --- | --- |
| `Optional<String>` (`ConfigurationSource`, `ValueResolver`, `sourceVal`) | nullable `String?` |
| `java.util.StringJoiner` | `joinToString(SEPARATOR)` |
| `java.util.regex.Pattern` | `kotlin.text.Regex` (works in common) |
| `Locale.ROOT` lowercasing | `String.lowercase()` (locale-independent in Kotlin) |
| `java.text.Normalizer` NFD (in `Name.normalizeSegment`) | **expect/actual** `normalizeForName()` |
| `System.getenv` (`EnvironmentValueResolver`) | **expect/actual** `platformGetEnv(name): String?` |
| `System.getProperties` (`SystemPropertiesValueResolver`) | **JVM-only** — keep in `jvmMain` |
| `ConcurrentHashMap` registry | removed with the singleton (see Phase 3) |
| `java.util.SortedMap` overloads (`ConfigurationSources.prioritized`) | Kotlin `Map`/ordered list |

- **expect/actual for normalization:** declare `internal expect fun normalizeForName(s: String): String`
  in commonMain. `jvmMain` actual = `java.text.Normalizer.normalize(s, NFD)` + strip. For native,
  either a best-effort actual (lowercase + regex strip, documenting that NFD decomposition is
  skipped) or pull in the KMP normalization lib `com.doist.x:normalize` as a `commonMain` dep to
  get true cross-platform NFD. **Recommend the doist lib** so naming behaves identically on all
  targets; fall back to best-effort if it complicates the build.
- **expect/actual for env:** `internal expect fun platformGetEnv(name: String): String?`;
  jvm actual = `System.getenv`; native actual = `platform.posix.getenv(name)?.toKString()`
  (available on all four desktop native targets).
- **`Name` / `Namespace`:** keep as classes implementing `Comparable`. Re-evaluate the
  `CharSequence` implementation — it's rarely useful and complicates a value-type design; **drop
  `CharSequence`** unless a concrete need surfaces. Provide `companion object` factories with
  `@JvmStatic` (`Name.of(...)`, `Namespace.of/root/parse`). Consider `@JvmInline value class`
  later, but a normal immutable class is simpler given the segment list + derived string.
- **`ValueConverter` / `DefaultValueConverter`:** straightforward port; keep primitive-returning
  methods. Booleans recognized: `true/yes/y/on/1`. Verify number parsing throws consistently
  (see correctness notes).

## Phase 3 — Idiomatic Kotlin API redesign

- **Drop `ConfigurationPropertiesRegistry`** (global mutable singleton — hostile to native &
  concurrency). Replace nested-config lookup with explicit composition: a parent constructs and
  holds its children directly (e.g. `val network = NetworkConfig(source, namespace.child("net"))`),
  instead of `nested(namespace, Type.class)` reaching into a global map.
- **Property delegates** as the primary Kotlin API on `ConfigurationProperties`:

  ```kotlin
  class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
      val serviceUrl: String by string("service", "url")
      val port: Int by int("port")
      val localMode: Boolean by boolean("local", "mode")
  }
  ```

  Implement `string()/int()/boolean()/long()/double()/short()/float()` as functions returning
  `ReadOnlyProperty<Any?, T>` (or `PropertyDelegateProvider` to capture the property name).
  Keep `stringVal(Name)` etc. as `protected` methods too, for Java subclasses.
- **Defaults & missing-value policy:** replace the `defaultsResolver` Function indirection with a
  clear `MissingValuePolicy` (throw vs null) and per-delegate default support
  (`by string("x") default "y"` or an overload). Preserve `isSet/isDefault/isMissing`.
- **`ConfigurationSources` factory:** Kotlin `object` with `@JvmStatic` `forEnvironment()`,
  `custom(...)`, `prioritized(...)`; `forSystemProperties()` lives in the jvm source set only.
  Add idiomatic Kotlin builders/`vararg` + DSL where it reads well.
- **Java-interop pass:** annotate facades with `@JvmStatic`, default-arg ctors with `@JvmOverloads`,
  and add `@JvmName` where Kotlin names collide with Java expectations. The JVM artifact must stay
  usable from Java (examples are currently Java).
- **Static code analysis:** Integrate [Detekt](https://detekt.dev/) to perform initial and
  ongoing static analysis.

## Phase 4 — Tests, examples, scaffolding

- **Tests:** port the 12 existing JUnit5/AssertJ/Mockito tests. Put platform-agnostic logic tests
  (`Name`, `Namespace`, `DefaultValueConverter`, source composition) in `commonTest` using
  `kotlin-test`. Keep JVM-specific tests (system-properties, env via `system-stubs`) in `jvmTest`.
  For env in `commonTest`, inject a fake env provider rather than mutating real env. Add at least
  one `nativeTest` to prove the native targets actually run.
  - Investigate using [Kotest](https://kotest.io/) for its KMP support.
  - Investigate more idiomatic mocking libraries, like [MockK](https://mockk.io/) or
    [Mokkery](https://mokkery.dev/).
- **Examples:** convert `examples/simple-config` and `examples/nested-config` — keep one as **Java**
  (proves interop) and rewrite one in **Kotlin** (shows delegates). Re-enable the examples
  convention plugin (JVM application) once the core API stabilizes.
- **KSP scaffold (deferred impl):** create `lena-config-ksp` module applying the KSP plugin with a
  stub `SymbolProcessorProvider`/`SymbolProcessor` and the `@ExternalConfiguration` annotation moved
  into `commonMain`. No generation logic yet — just compiles and is wired into the build. Remove the
  old `lena-config-annotation-processor` Java/aptk experiment and its generated sources.

## Correctness review (do during the port, note findings)

- `Name`/`Namespace` normalization + separator handling — verify round-tripping and the empty/root
  edge cases the tests cover.
- `DefaultValueConverter`: confirm null→0 vs throw-on-invalid behavior is intended and consistent
  across types; the boolean set is asymmetric (no explicit false list) — confirm "anything else =
  false" is desired.
- `ConfigurationProperties` constructor calls `createChildren()` and registry registration during
  construction (open-method-in-constructor smell) — the redesign removes this hazard.
- `prioritized(SortedMap)` ordering semantics — make ordering explicit (insertion order / comparator)
  rather than relying on `SortedMap`.

## Files

- Build: `gradle/wrapper/gradle-wrapper.properties`, `gradle/libs.versions.toml`,
  `settings.gradle.kts`, `buildSrc/build.gradle.kts`, `buildSrc/src/main/kotlin/*.gradle.kts`.
- Core (move Java → `lena-config/src/commonMain/kotlin/org/riversoforion/lena/config/`, with
  `jvmMain`/`nativeMain` actuals): `Name`, `Namespace`, `NameResolver`, `ValueResolver`,
  `ConfigurationSource`, `Simple/Composite/PrioritizedConfigurationSource`, `ConfigurationSources`,
  `ConfigurationProperties`, `ValueConverter`, `DefaultValueConverter`, `resolvers/*`.
- Delete: `ConfigurationPropertiesRegistry`, old `lena-config-annotation-processor/` tree,
  the three Java convention plugins.
- Rework `CLAUDE.md` (see below).

## CLAUDE.md rework (do as part of implementation)

The current `CLAUDE.md` describes the Java/`Optional`/registry design that this overhaul removes —
leaving it will actively mislead future sessions. Replace it to describe: KMP layout
(`commonMain`/`jvmMain`/`nativeMain`), desktop-tier targets, Kotlin 2.4 + Gradle 9 + KSP, the
property-delegate API, `String?`-not-`Optional`, expect/actual seams (env + normalization), and
the deferred-processor status. Add KMP build/test commands
(`./gradlew :lena-config:jvmTest`, `:lena-config:macosArm64Test`, `allTests`).

## Verification

1. `./gradlew build` — all declared targets compile (`compileKotlinJvm`,
   `compileKotlinMacosArm64`, `…LinuxX64`, `…MingwX64`, `…MacosX64`).
2. `./gradlew allTests` — common + jvm + at least one native test suite pass; confirm `commonTest`
   logic tests run on a native target (proves no JVM-only leakage in `commonMain`).
3. `./gradlew :lena-config:jvmTest` — ported JUnit5/AssertJ suite green.
4. Build the Java example against the JVM artifact — confirms `@JvmStatic`/interop ergonomics.
5. Run the Kotlin example — confirms the property-delegate API end to end.
6. Spot-check `platformGetEnv` + normalization on a native target (e.g. small `nativeTest`
   asserting an env var read and an accented-name normalization).

## Next steps (post-migration)

These items are out of scope for the current overhaul but should be explored once Phases 1–4
are complete and the KMP foundation is stable.

### FederatedConfigurationSource

Add `FederatedConfigurationSource` to `lena-config`. It routes `(Namespace, Name)` lookups to
the most-specific registered backend by namespace prefix, making heterogeneous source composition
explicit and transparent to `ConfigurationProperties` subclasses:

```kotlin
val appSource = FederatedConfigurationSource()
appSource.mount(Namespace.root(),      ConfigurationSources.prioritized(env, sysProps))
appSource.mount(Namespace.of("vault"), vaultSource)
appSource.mount(Namespace.of("db"),    databaseSource)
```

This is also the natural seam for future **writable config sources** — a `WritableConfigurationSource`
sub-interface whose writes the federated source delegates to the appropriate backend.

Decide at design time: immutable-after-construction vs. mutable (supporting runtime source
changes such as Vault lease refresh).

### ConfigurationContext (separate module)

Explore a `lena-config-context` module that provides an explicit, application-scoped registry
of `ConfigurationProperties` instances keyed by namespace. This covers the original registry's
goals — single canonical instance per "configuration coordinates," cross-subsystem access
without direct coupling — without embedding a hidden global singleton in the library.

Key design questions to resolve:

- Lifecycle: should the context own construction of properties objects, or just track instances
  that register themselves?
- Scope: single process-wide context, or composable/hierarchical (child context inherits from
  parent, useful for multi-tenant or test isolation scenarios)?
- Integration with `FederatedConfigurationSource`: the context and the federated source likely
  complement each other — the source handles *where values come from*, the context handles
  *who holds the canonical view*.

### Kotlin/WASM and Kotlin/JS targets

Investigate whether `lena-config` is viable on Kotlin/WASM and Kotlin/JS targets. Key
questions:

- **Environment access:** neither WASM nor JS has a POSIX `getenv`. A new `expect`/`actual`
  seam (or a distinct `wasmJsMain` source set) would be needed — likely returning `null` for
  all env lookups, or delegating to a platform-provided callback.
- **String normalization:** browser/WASM runtimes may have Unicode normalization available via
  the JS `String.prototype.normalize()` API; investigate whether Kotlin/JS can call this to
  provide true NFD normalization rather than the native best-effort implementation.
- **Use cases:** config in a browser or WASM context is typically injected at build time or via
  a backend API rather than read from environment variables — consider whether a read-only,
  map-backed source is the right primitive for these targets rather than the env/properties
  resolvers.
- **Tier:** Kotlin/WASM (`wasmJs`) is a Tier 2 target as of Kotlin 2.x; Kotlin/JS is Tier 1.
  Evaluate stability and toolchain maturity before committing to either.
