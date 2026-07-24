# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lena is a Kotlin Multiplatform library collection focused on type-safe application configuration.
The primary active module is `lena-config`, which provides a hierarchical, namespace-aware
configuration system that reads from environment variables and/or system properties. Core logic
lives in `commonMain` and targets JVM plus desktop-tier native (macOS, Linux, Windows). Java
interop on the JVM is a first-class concern (`@JvmStatic`, `@JvmOverloads`, protected accessor
methods alongside Kotlin property delegates).

See `docs/kotlin-migration-plan.md` for the full migration plan, phase history, and forward
roadmap (KSP annotation processor, type coercion, validation/observability).

## Build Commands

```bash
# Build everything
./gradlew build

# Run all tests (common + jvm + native targets)
./gradlew :lena-config:allTests

# Run just the JVM test suite
./gradlew :lena-config:jvmTest

# Run a native test suite (host-dependent; e.g. on macOS)
./gradlew :lena-config:macosArm64Test

# Run a single JVM test class
./gradlew :lena-config:jvmTest --tests "org.riversoforion.lena.config.NameTest"

# Run examples (forwards -D/-P properties to the example JVM)
./gradlew :examples:simple-config:exec
./gradlew :examples:nested-config:exec
```

## Module Structure

The root project is `lena`. Gradle subprojects:

| Module | Status | Purpose |
| --- | --- | --- |
| `lena-config` | Active | Core configuration library (Kotlin Multiplatform: `commonMain`/`jvmMain`/`nativeMain`) |
| `lena-config-ksp` | Scaffold only | KSP module for `@ExternalConfiguration` processing — compiles and wires in, but generates nothing yet (deferred; see migration plan Phase 5). KSP is `kotlinc`-only — consumers on a `javac`-only build (no Kotlin Gradle plugin) cannot use it; see migration plan Phase 5 and "Deprioritized: Future Explorations" |
| `examples/common` | Active | Shared Java base class (`ExampleApplication`) for runnable examples |
| `examples/simple-config` | Active | Java example — proves JVM interop against the Kotlin library |
| `examples/nested-config` | Active | Kotlin example — demonstrates property delegates and explicit nested composition |
| `lena-config-aws-secretsmanager`, `lena-config-cache`, `lena-config-consul`, `lena-config-dotenv`, `lena-config-etcd`, `lena-config-sql`, `lena-config-zookeeper`, `lena-junit5` | Placeholder | Empty — not yet implemented |

## Convention Plugins (buildSrc)

Precompiled script plugins in `buildSrc/src/main/kotlin/`:

- **`lena.kmp-library`** — Applies `org.jetbrains.kotlin.multiplatform` + Detekt. Declares JVM +
  desktop-tier native targets (`macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64`), `explicitApi()`,
  `-Xexpect-actual-classes`, `maven-publish`/`signing`. Used by `lena-config`.
- **`lena.examples`** — Applies `org.jetbrains.kotlin.jvm` + `application`. Registers an `exec`
  task (distinct from the `application` plugin's `run`) that forwards user-supplied `-D`/`-P`
  properties from the Gradle invocation to the example JVM — filtered against a blocklist of
  JVM/OS-owned property prefixes (`java.`, `os.`, `sun.`, etc.), since forwarding those (e.g.
  `java.home`) corrupts a child JVM running a different JDK than the Gradle daemon. Used by
  `examples/simple-config` and `examples/nested-config`.

`examples/common` and `lena-config-ksp` apply `org.jetbrains.kotlin.jvm` directly (no shared
convention plugin needed for a single non-application module).

Note: buildSrc precompiled script plugins do **not** see the root project's `libs` version catalog
accessor — dependency coordinates needed inside a `.gradle.kts` convention plugin (as opposed to
inside a module's own `build.gradle.kts`) are written as literal strings.

## Core Architecture (lena-config)

The design separates *naming* from *lookup* and composes them into sources.

### Key types

- **`Name`** — Immutable, normalized hierarchical property name. Segments are slash-separated;
  `toString()` is normalized (trimmed, lowercased, non-alphanumeric runs → `-`; NFD-decomposed on
  JVM, best-effort ASCII-strip on native — see "Platform differences" below). `segments()` returns
  the *raw*, unnormalized segments. `Name.of("service", "url")` → `"service/url"`.
- **`Namespace`** — Scoping prefix, also slash-separated, always starting with `/`.
  `Namespace.root()` is `"/"`. Combines with `Name` to form a full lookup path.
- **`NameResolver`** — `fun interface`; converts a `(Namespace, Name)` pair into the string key
  used for lookup (e.g., env var format `SERVICE_URL` or property format `service.url`).
- **`ValueResolver`** — `fun interface`; looks up a value by its resolved key string.
- **`ConfigurationSource`** — `fun interface`; `getValue(Namespace, Name): String?`. The single
  point of truth for reading config values. Nullable, not `Optional`.
- **`SimpleConfigurationSource`** — Combines one `NameResolver` + one `ValueResolver`.
- **`CompositeConfigurationSource`** — Wraps a source with an optional namespace prefix (used for
  scoped sub-configs).
- **`PrioritizedConfigurationSource`** — Tries sources in order, returning the first present value.
- **`ConfigurationSources`** — `object` with `@JvmStatic` factories: `forEnvironment()`,
  `custom(...)`, `prioritized(...)` (all targets); `forSystemProperties()` (JVM-only, in `jvmMain`).
- **`ConfigurationProperties`** — Abstract base class for user-defined config beans. Two APIs:
  - **Kotlin**: property delegates — `val port: Int by int("port")`, with `optional*` (nullable)
    and `default = ...` overloads for every primitive type (`string`, `boolean`, `short`, `int`,
    `long`, `float`, `double`).
  - **Java**: protected accessor methods — `stringVal(Name)`, `intVal(Name)`, `booleanVal(Name)`,
    etc., plus `sourceVal`/`optionalStringVal` for nullable raw access.
  - Non-nullable delegates/accessors throw `MissingConfigurationException` when absent and no
    default is given; nullable (`optional*`) variants return `null`.
- **`MissingConfigurationException`** — Thrown by non-nullable accessors on a missing value.

There is **no global registry**. Nested configuration is explicit composition — a parent
constructs and holds its children directly, sharing one backing `ConfigurationSource`:

```kotlin
class AppConfig(
    ns: Namespace = Namespace.root(),
    private val backingSource: ConfigurationSource = prioritized(forEnvironment(), forSystemProperties()),
) : ConfigurationProperties(backingSource, ns) {
    val service = ServiceConfig(backingSource, namespace.child("service"))
    val port: Int by int("port")
}
```

### Name → key resolution

Given `Namespace.of("app")` and `Name.of("service", "url")`:

- Environment variable: `APP_SERVICE_URL` (underscore-joined, uppercased)
- System property: `app.service.url` (dot-joined, lowercased)

At `Namespace.root()`, only the `Name` segments are used.

### Extending ConfigurationProperties

Kotlin (primary API):

```kotlin
class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    val serviceUrl: String by string("service", "url")
    val port: Int by int("port")
    val tag: String? by optionalString("tag")
    val timeout: Long by long("timeout", default = 5_000L)
}
```

Java (protected accessors):

```java
public class AppConfig extends ConfigurationProperties {
    public AppConfig(ConfigurationSource source) { super(source); }
    public String serviceUrl() { return stringVal(Name.of("service", "url")); }
    public int port()          { return intVal(Name.of("port")); }
}
```

### Platform differences (expect/actual seams)

- **Environment access** (`internal expect fun platformGetEnv`): JVM uses `System.getenv`; native
  uses POSIX `getenv`.
- **String normalization** (`internal expect fun normalizeForName`, used by `Name`/`Namespace`
  segment normalization): JVM uses `java.text.Normalizer` (NFD) — accented characters decompose to
  their base letter (`é` → `e`). Native has no `Normalizer`; it strips non-ASCII characters
  outright without decomposition (`é` is dropped, not reduced) — a documented limitation. Tests
  covering this divergence live in `jvmTest`/`nativeTest`, not `commonTest`.

## Testing

- **`commonTest`**: platform-agnostic logic (`Name`, `Namespace`, `DefaultValueConverter`, source
  composition, `ConfigurationProperties` delegates) using `kotlin.test` — table-driven loops
  rather than JUnit5 `@ParameterizedTest`, since that annotation doesn't run on native targets.
  Fakes/lambdas stand in for mocks (`NameResolver`/`ValueResolver`/`ConfigurationSource` are `fun
  interface`s), since Mockito is JVM-only.
- **`jvmTest`**: JVM-specific behavior (`System.getenv`/`System.getProperties` via
  `system-stubs-jupiter`, JVM `NumberFormatException` message format, NFD normalization) using
  JUnit 5 + AssertJ + Mockito.
- **`nativeTest`**: at least one test (`NameNormalizationNativeTest`) proving native targets
  actually execute `commonMain` logic, and documenting the native-specific normalization behavior.
- Gradle 9's `Test` task requires `org.junit.platform:junit-platform-launcher` explicitly on the
  runtime classpath (not just the engine) — wired into `lena.kmp-library` (`lena-config`'s jvmTest)
  and `lena.examples`.

Run everything with `./gradlew :lena-config:allTests`; JVM-only with `:lena-config:jvmTest`.
