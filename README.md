# Lena

Lena is a Kotlin Multiplatform library collection intended to provide a range of useful utilities
for Kotlin and Java applications — configuration, and (per the placeholder modules below) caching,
secrets management, and integrations with common backends. Right now, the only implemented area is
application configuration.

The active module, `lena-config`, gives you a hierarchical, namespace-aware configuration system
that reads from environment variables and/or system properties (with a pluggable source model for
anything else), and exposes values through typed accessors — Kotlin property delegates or
protected Java accessor methods, your choice.

```kotlin
class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    val serviceUrl: String by string("service", "url")
    val port: Int by int("port")
    val timeout: Long by long("timeout", default = 5_000L)
    val apiKey: String? by optionalString("api", "key")
}
```

```java
public class AppConfig extends ConfigurationProperties {
    public AppConfig(ConfigurationSource source) { super(source); }
    public String serviceUrl() { return stringVal(Name.of("service", "url")); }
    public int port()          { return intVal(Name.of("port")); }
}
```

> **Status:** pre-1.0, actively developed. The core library (`lena-config`) is functional and
> tested across JVM and desktop-tier native targets. Several planned modules are still empty
> placeholders — see [Module Status](#module-status) below.

## Table of Contents

- [Module Status](#module-status)
- [Requirements](#requirements)
- [Using lena-config](#using-lena-config)
- [Running the Examples](#running-the-examples)
- [Developing Lena](#developing-lena)
- [Documentation](#documentation)
- [License](#license)

## Module Status

| Module | Status | Purpose |
| --- | --- | --- |
| `lena-config` | **Active** | Core configuration library (Kotlin Multiplatform: `commonMain`/`jvmMain`/`nativeMain`) |
| `lena-config-ksp` | Scaffold only | KSP module for `@ExternalConfiguration` processing — compiles and wires in, generates nothing yet. **Note:** KSP is a `kotlinc`-only mechanism; pure-Java, `javac`-only builds (no Kotlin Gradle plugin) will not be able to use it — see [`docs/kotlin-migration-plan.md`](docs/kotlin-migration-plan.md#implementation-ksp-processor) |
| `examples/common`, `examples/simple-config`, `examples/nested-config` | **Active** | Runnable usage examples (see below) |
| `lena-config-aws-secretsmanager`, `lena-config-cache`, `lena-config-consul`, `lena-config-dotenv`, `lena-config-etcd`, `lena-config-sql`, `lena-config-zookeeper`, `lena-junit5` | Placeholder | Empty — not yet implemented |

## Requirements

- JDK 21 (Gradle will resolve/download a toolchain via the `foojay-resolver` plugin if you don't
  have one)
- No local Gradle install needed — use the wrapper (`./gradlew`)

`lena-config` compiles for the JVM and desktop-tier native targets (`macosArm64`, `linuxX64`,
`linuxArm64`, `mingwX64`); native tests only run on a matching host (e.g. `macosArm64Test` on
Apple Silicon macOS).

## Using lena-config

`lena-config` isn't published to Maven Central yet — consume it as a project dependency within
this build, or `./gradlew publishToMavenLocal` and depend on `com.riversoforion.lena:lena-config`
from another local project.

### Core concepts

- **`Name`** — an immutable, normalized property name (`Name.of("service", "url")` → `service/url`).
- **`Namespace`** — a scoping prefix (`Namespace.of("app")` → `/app`), composed with a `Name` to
  form a lookup key.
- **`ConfigurationSource`** — where values actually come from. Built-in sources:
  `ConfigurationSources.forEnvironment()`, `.forSystemProperties()` (JVM-only), `.custom(...)`,
  and `.prioritized(...)` for first-match-wins composition across multiple sources.
- **`ConfigurationProperties`** — the base class you extend to declare a typed config bean.

Given `Namespace.of("app")` and `Name.of("service", "url")`, the environment variable resolves to
`APP_SERVICE_URL` and the system property to `app.service.url`.

### Kotlin

```kotlin
import org.riversoforion.lena.config.ConfigurationProperties
import org.riversoforion.lena.config.ConfigurationSource
import org.riversoforion.lena.config.ConfigurationSources.forEnvironment
import org.riversoforion.lena.config.ConfigurationSources.forSystemProperties
import org.riversoforion.lena.config.ConfigurationSources.prioritized

class AppConfig(
    source: ConfigurationSource = prioritized(forEnvironment(), forSystemProperties()),
) : ConfigurationProperties(source) {
    val serviceUrl: String by string("service", "url")
    val port: Int by int("port", default = 8080)
    val tag: String? by optionalString("tag")
}
```

Non-nullable delegates throw `MissingConfigurationException` if the value is absent and no
default is given; `optional*` delegates return `null` instead. Nested configuration is explicit —
a parent constructs and holds its children directly, sharing one backing source:

```kotlin
class AppConfig(
    ns: Namespace = Namespace.root(),
    private val backingSource: ConfigurationSource = prioritized(forEnvironment(), forSystemProperties()),
) : ConfigurationProperties(backingSource, ns) {
    val service = ServiceConfig(backingSource, namespace.child("service"))
}
```

### Java

```java
import org.riversoforion.lena.config.ConfigurationProperties;
import org.riversoforion.lena.config.Name;

import static org.riversoforion.lena.config.ConfigurationSources.*;

public class AppConfig extends ConfigurationProperties {
    public AppConfig() {
        super(prioritized(forEnvironment(), forSystemProperties()));
    }

    public String serviceUrl() { return stringVal(Name.of("service", "url")); }
    public int port()          { return intVal(Name.of("port")); }
}
```

Full worked examples (including nested composition) live under [`examples/`](./examples).

## Running the Examples

Each example is a standalone CLI exposed via a Gradle `exec` task:

```shell
./gradlew examples:simple-config:exec
./gradlew examples:nested-config:exec
```

Supply configuration via environment variables or `-D`/`-P` Gradle properties — both are
forwarded through to the example JVM:

```shell
SERVICE_URL="http://localhost:8080" ./gradlew examples:simple-config:exec
./gradlew examples:simple-config:exec -Dservice.url="http://localhost:8080"
```

See [`examples/README.md`](examples/README.md) for the full catalog, supported flag syntax, and
details on how property forwarding works.

## Developing Lena

```shell
# Build everything
./gradlew build

# Run all lena-config tests (common + jvm + native targets)
./gradlew :lena-config:allTests

# Run just the JVM test suite
./gradlew :lena-config:jvmTest

# Run a single JVM test class
./gradlew :lena-config:jvmTest --tests "org.riversoforion.lena.config.NameTest"

# Static analysis (Detekt)
./gradlew :lena-config:detekt
```

Source is organized as Kotlin Multiplatform source sets:

- `commonMain` — platform-agnostic core logic
- `jvmMain` — JVM-specific pieces (`System.getProperties()` support, `java.text.Normalizer`)
- `nativeMain` — native-specific pieces (POSIX `getenv`, best-effort string normalization)
- `commonTest` / `jvmTest` / `nativeTest` — mirrored test source sets; see
  [`AGENTS.md`](AGENTS.md#testing) for the testing strategy (why some tests live in one source set
  and not another).

Convention plugins in `buildSrc/src/main/kotlin/` (`lena.kmp-library`, `lena.examples`) hold the
shared build configuration — see [`AGENTS.md`](AGENTS.md#convention-plugins-buildsrc) for details.

## Documentation

- [`AGENTS.md`](AGENTS.md) — architecture reference: core types, name/key resolution, platform
  differences, testing strategy
- [`docs/kotlin-migration-plan.md`](docs/kotlin-migration-plan.md) — the Java → Kotlin Multiplatform
  migration plan, phase history, and forward roadmap (KSP annotation processing, type coercion,
  validation/observability)
- [`examples/README.md`](examples/README.md) — example catalog and usage

## License

Apache License 2.0 — see [`LICENSE`](LICENSE).
