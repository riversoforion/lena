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

## Phase 5 — Complete the KSP Annotation Processor (Foundation)

Once Phase 4 is complete and the core API is stable, prioritize finishing the deferred annotation
processor. This is the critical bridge to Java ergonomics and enables property reflection.

### Rationale: KSP Processor

The old Java API requires boilerplate:

```java
public class AppConfig extends ConfigurationProperties {
    public AppConfig(ConfigurationSource source) { super(source); }
    public String serviceUrl() { return stringVal(Name.of("service", "url")); }
    public int port() { return intVal(Name.of("port")); }
}
```

With a KSP processor, users write a clean interface:

```java
@ExternalConfiguration
public interface AppConfig {
    @ConfigurationProperty("service/url")
    String serviceUrl();
    
    @ConfigurationProperty("port")
    int port();
}
```

The processor generates the `ConfigurationProperties` subclass. Benefits:

- **Eliminates boilerplate:** declare, not implement
- **IDE support:** autocomplete on the interface; properties are discoverable
- **Reflection:** processor knows all properties (enables doc generation, validation schemas)
- **Java parity:** matches Spring `@ConfigurationProperties` ergonomics
- **Kotlin support:** generator can produce data classes with delegates for Kotlin users

### Implementation: KSP Processor

1. Move `@ExternalConfiguration` and `@ConfigurationProperty` from temporary location into stable API.
2. Create `lena-config-ksp` module with `SymbolProcessor` implementation:
   - Read `@ExternalConfiguration` interfaces
   - For each `@ConfigurationProperty` method, derive the `Name` and return type
   - Generate a `ConfigurationProperties` subclass with typed accessors
   - Store metadata (property name, type, default value) for reflection
3. Wire into `lena-config` build as an optional annotation processor (declare as dependency for
   consumers who want compile-time generation).
4. Provide a `PropertyRegistry` utility so generated classes can expose all their properties for
   introspection (used by doc generation, validation schema builders, etc.).

### Verification: KSP Processor

- Processor compiles on JVM + Kotlin 2.4 + KSP 2.4.x
- Generated code compiles cleanly
- Generated code passes existing tests (reuse test infrastructure from Phase 4)
- Example: write a Java interface, run KSP, generated class works end-to-end

---

## Phase 6 — Type Coercion & Complex Types (Power)

After the processor is working, expand value coercion beyond primitives + strings.

### Current Limitation

Today, users work only with primitives + strings:

```kotlin
val timeoutMs: Int by int("timeout")
val timeout = Duration.ofMillis(timeoutMs.toLong())  // manual step

val portsStr: String by string("ports")
val ports = portsStr.split(",").map { it.toInt() }  // manual step
```

### Target API

```kotlin
val timeout: Duration by duration("timeout")
val ports: List<Int> by intList("ports")  // auto-splits on comma
val logLevel: LogLevel by enum("log", "level")
val apiUrl: URL by converted("api", "url") { URL(it) }
```

### Implementation: Type Coercion

1. **Extend `ValueConverter`:** add `@Suppress("TooManyFunctions")` and implement:
   - `toDate(String?): LocalDate` / `toDateTime(String?): OffsetDateTime`
   - `toDuration(String?): Duration` (parse ISO-8601 or shorthand like "5s", "2m")
   - `toUuid(String?): UUID`
   - `toUrl(String?): URL`
   - `toEnum(String?, klass: KClass<T>): T`

2. **Add collection support:**
   - `toIntList(String?): List<Int>` (splits on comma, trim, parse each)
   - `toStringSet(String?): Set<String>`
   - `toStringMap(String?): Map<String, String>` (parse key=value pairs)

3. **Add delegate variants** for all new converters (parallel to `int()`, `boolean()`, etc.):
   - `duration()`, `durationList()`, `duration(default = ...)`
   - `enum<T>()`, `enum<T>(default = ...)`
   - `url()`, `uuid()`, `converted { ... }`

4. **Pluggable converters:** expose a way for users to register custom converters:

```kotlin
class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    init {
        converter.register<CustomType> { CustomType.parse(it) }
    }
}
```

### Verification: Type Coercion

- `Duration by duration("timeout")` correctly parses ISO-8601 and shorthand
- `List<String> by stringList("hosts")` splits comma-separated values
- Custom converter hook works: `URL by converted("api/url") { URL(it) }`
- Edge cases: empty strings, null, invalid formats throw appropriately

---

## Phase 7 — Validation & Observability (Quality)

Add runtime guardrails and visibility into configuration decisions.

### Validation DSL

```kotlin
class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    val port: Int by int("port") validate { it in 1..65535 }
    val timeout: Duration by duration("timeout") validate { it.seconds > 0 }
    val name: String by string("app", "name") validate { it.isNotBlank() }
}
```

Validation failures throw on property access (lazy, not at construction).

### Implementation: Validation

1. Add `validate { ... }` extension on delegates (returns a wrapper delegate that intercepts access)
2. Collect validation errors and expose via:
   - `isValid(): Boolean`
   - `validationErrors(): List<ValidationError>`
3. Support for JSR-380 (Jakarta Bean Validation) annotations on generated interfaces
   (via KSP Phase 5 processor — `@Min(1)`, `@Max(65535)`, etc.)

### Observability Hooks

```kotlin
class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    init {
        onPropertyRead { name, value, sourceUsed ->
            logger.debug("Config read: $name=$value from $sourceUsed")
        }
        onPropertyMissing { name ->
            logger.warn("Missing configuration property: $name (using default or null)")
        }
        onValidationError { name, error ->
            logger.error("Validation failed for $name: $error")
        }
    }
}
```

Use cases:

- **Startup diagnostics:** log all properties read during init, warn about missing ones
- **Testing:** mock/capture reads to verify expected properties are checked
- **Metrics:** send to observability system (how many reads from env vs properties, error rates)
- **Audit:** record which properties are accessed for compliance/debugging

### Implementation: Observability

1. Add optional `onPropertyRead`, `onPropertyMissing`, `onValidationError` callbacks to
   `ConfigurationProperties`
2. Invoke callbacks at appropriate points (in delegates, in validators)
3. Provide a built-in logger callback for convenience
4. Expose property metadata via Phase 5 processor for static analysis (which properties should
   exist, which are required, which have defaults)

### Verification: Validation & Observability

- Validation error is thrown on property access if constraint violated
- Observability callbacks fire with correct name/value/source
- Integration test: configure a mock observer, assert expected properties are read

---

## Deprioritized: Future Explorations

The following items are valuable but not core to the positioning. Revisit after Phase 7:

### FederatedConfigurationSource

A higher-level abstraction for routing lookups by namespace prefix:

```kotlin
val appSource = FederatedConfigurationSource()
appSource.mount(Namespace.root(),      ConfigurationSources.prioritized(env, sysProps))
appSource.mount(Namespace.of("vault"), vaultSource)
appSource.mount(Namespace.of("db"),    databaseSource)
```

**Why it's deprioritized:** It's a convenience layer over `PrioritizedConfigurationSource`. Users
needing namespace-based routing can already do it manually. This is a quality-of-life feature
worth exploring *after* you've validated that users actually ask for this pattern.

**When to revisit:** After Phase 7, if you've seen users implement this pattern independently
multiple times, it becomes a candidate for the library.

### ConfigurationContext (Application-scoped registry)

A `lena-config-context` module providing an explicit, opt-in registry:

```kotlin
val context = ConfigurationContext()
val appConfig = AppConfig(source)
context.register(Namespace.root(), appConfig)
val retrieved = context.resolve(Namespace.root(), AppConfig::class)
```

**Why it's deprioritized:** It's a workaround for the global singleton that was intentionally
removed. Java developers missing the convenience should instead use their existing DI container
(Spring, Dagger, Koin, Guice). Lena should remain small; lifecycle management belongs in the
application framework, not the config library.

**Alternative:** After Phase 5, document how to integrate with popular DI containers so users
can manage `ConfigurationProperties` instances without a global registry.

### Kotlin/WASM and Kotlin/JS targets

Expanding to browser/serverless JavaScript contexts.

**Why it's deprioritized:** These platforms are out of scope for the "desktop-tier" positioning
(JVM, macOS, Linux, Windows native). WASM/JS have fundamentally different configuration patterns
(injected at build time, via environment at runtime, or fetched from a backend API) than what
`lena-config` optimizes for (reading env vars + system properties).

**Alternative:** If Lena users request WASM/JS support, evaluate it then. But the core value prop
(native + JVM) is strong enough without it.

---

## Strategic Positioning (Post-Phase 7)

Once Phases 5–7 are complete, Lena's value proposition is:

> **Multiplatform, type-safe configuration with compile-time validation and strong observability.
> Kotlin-first with excellent Java interop via annotation processing. Built-in support for
> complex types (Duration, UUID, Lists, Enums). No magic, no global state, full transparency
> into configuration decisions.**

This positions Lena for:
- **Kotlin/Native applications** needing configuration (currently unique in this space)
- **JVM teams** wanting Spring-style ergonomics without Spring overhead
- **Cloud-native and embedded systems** where observability and validation at config time are critical
- **Polyglot shops** where Kotlin can coexist with Java without friction
