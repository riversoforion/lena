# Phase 5 — Complete the KSP Annotation Processor (Foundation)

**Status:** Deferred (scaffold only — `lena-config-ksp` compiles and wires into the build but
generates nothing yet; see `AGENTS.md` module table)

Once Phase 4 is complete and the core API is stable, prioritize finishing the deferred annotation
processor. This is the critical bridge to Java ergonomics and enables property reflection.

## Rationale

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

## Implementation

1. Move `@ExternalConfiguration` and `@ConfigurationProperty` from temporary location into stable API.
2. Create `lena-config-ksp` module with `SymbolProcessor` implementation:
   - Read `@ExternalConfiguration` interfaces
   - For each `@ConfigurationProperty` method, derive the `Name` and return type
   - Generate a `ConfigurationProperties` subclass with typed accessors
   - Store metadata (property name, type, default value) for reflection
3. Wire into `lena-config` build as an optional annotation processor (declare as dependency for
   consumers who want compile-time generation).
   - **`javac`/APT incompatibility:** KSP is a `kotlinc`-only mechanism — it cannot be registered
     with, or invoked by, plain `javac`/`javax.annotation.processing`. Consumers on a pure-Java,
     `javac`-only build (no Kotlin Gradle plugin at all) cannot use `lena-config-ksp`; they must
     apply `kotlin("jvm")` (or `multiplatform`) and add `ksp(project(":lena-config-ksp"))` so
     `kotlinc` compiles the module instead — even if every `@ExternalConfiguration` interface is
     written in `.java`. This is a Kotlin *compiler*-toolchain dependency, not a Kotlin
     *source-language* one, but it is a real barrier for teams that refuse to add the Kotlin
     Gradle plugin. A separate, `javac`-compatible APT processor is tracked as a deferred,
     community-driven enhancement (see [Deprioritized: Future Explorations](deprioritized.md)).
4. Provide a `PropertyRegistry` utility so generated classes can expose all their properties for
   introspection (used by doc generation, validation schema builders, etc.).

## Verification

- Processor compiles on JVM + Kotlin 2.4 + KSP 2.4.x
- Generated code compiles cleanly
- Generated code passes existing tests (reuse test infrastructure from Phase 4)
- Example: write a Java interface, run KSP, generated class works end-to-end
