# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lena is a Java 21 library collection focused on type-safe application configuration. The primary active module is `lena-config`, which provides a hierarchical, namespace-aware configuration system that reads from environment variables and/or system properties.

## Build Commands

```bash
# Build everything
./gradlew build

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :lena-config:test
./gradlew :lena-config-annotation-processor:test

# Run a single test class
./gradlew :lena-config:test --tests "org.riversoforion.lena.config.NameTest"

# Run examples
./gradlew :examples:simple-config:exec
./gradlew :examples:nested-config:exec
```

## Module Structure

The root project is `lena`. Gradle subprojects:

| Module | Status | Purpose |
| --- | --- | --- |
| `lena-config` | Active | Core configuration library |
| `lena-config-annotation-processor` | In progress | `@ExternalConfiguration` annotation processor |
| `examples/common`, `examples/simple-config`, `examples/nested-config` | Active | Runnable usage examples |
| `lena-config-aws-secretsmanager`, `lena-config-cache`, `lena-config-consul`, `lena-config-dotenv`, `lena-config-etcd`, `lena-config-sql`, `lena-config-zookeeper`, `lena-junit5` | Placeholder | Empty — not yet implemented |

## Convention Plugins (buildSrc)

All modules use one of three convention plugins defined in `buildSrc/src/main/kotlin/`:

- **`lena.java-conventions`** — Base plugin: Java 21 toolchain, JVM args for Mockito/ByteBuddy (`-XX:+EnableDynamicAgentLoading`), JUnit platform test runner.
- **`lena.libraries`** — Extends `lena.java-conventions`: adds `java-library`, `maven-publish`, and `signing` for publishable library modules.
- **`lena.examples`** — Extends `lena.java-conventions`: adds `application` and a `exec` task that passes system properties through to the JVM.

Library modules declare `id("lena.libraries")`. Example modules declare `id("lena.examples")`.

## Core Architecture (lena-config)

The design separates *naming* from *lookup* and composes them into sources.

### Key types

- **`Name`** — Immutable, normalized hierarchical property name. Segments are slash-separated, lowercased, and stripped of non-alphanumeric characters. `Name.of("service", "url")` → `"service/url"`.
- **`Namespace`** — Scoping prefix, also slash-separated. `Namespace.root()` is the empty namespace (`"/"`). Namespaces combine with `Name` to form a full lookup path.
- **`NameResolver`** — Converts a `(Namespace, Name)` pair into the string key used for lookup (e.g., env var format `SERVICE_URL` or property format `service.url`).
- **`ValueResolver`** — Performs the actual value lookup given a resolved key string.
- **`ConfigurationSource`** — Core interface: `getValue(Namespace, Name) → Optional<String>`. The single point of truth for reading config values.
- **`SimpleConfigurationSource`** — Combines one `NameResolver` + one `ValueResolver`.
- **`CompositeConfigurationSource`** — Wraps a source with a namespace prefix (used for scoped sub-configs).
- **`PrioritizedConfigurationSource`** — Tries sources in order, returning the first present value.
- **`ConfigurationSources`** — Factory class with static helpers: `forEnvironment()`, `forSystemProperties()`, `custom(...)`, `prioritized(...)`.
- **`ConfigurationProperties`** — Abstract base class for user-defined config beans. Subclass this, call `super(source)` or `super(source, namespace)`, then expose typed accessors via `stringVal(Name)`, `intVal(Name)`, `booleanVal(Name)`, etc.
- **`ConfigurationPropertiesRegistry`** — Singleton that auto-registers every `ConfigurationProperties` instance on construction, enabling nested config lookup via `nested(namespace, Type.class)`.

### Name → key resolution

Given `Namespace.of("app")` and `Name.of("service", "url")`:

- Environment variable: `APP_SERVICE_URL` (underscore-joined, uppercased)
- System property: `app.service.url` (dot-joined, lowercased)

At `Namespace.root()`, only the `Name` segments are used.

### Extending ConfigurationProperties

```java
public class AppConfig extends ConfigurationProperties {
    public AppConfig() {
        super(ConfigurationSources.prioritized(
            ConfigurationSources.forEnvironment(),
            ConfigurationSources.forSystemProperties()
        ));
    }

    public String serviceUrl() { return stringVal(Name.of("service", "url")); }
    public int port()          { return intVal(Name.of("port")); }
}
```

Call `returnNullForMissing()` in the constructor if missing values should return `null` instead of throwing.

## Testing

Tests use JUnit 5 + AssertJ + Mockito. The `system-stubs-jupiter` extension (`uk.org.webcompere:system-stubs-jupiter`) is available for stubbing environment variables and system properties in tests.

Version catalogs are generated from BOM coordinates in `gradle/libs.versions.toml` using the `dev.aga.gradle.version-catalog-generator` plugin. The generated catalogs are named `junitPlatform`, `assertJPlatform`, and `mockitoPlatform`.
