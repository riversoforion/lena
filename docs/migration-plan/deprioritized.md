# Deprioritized: Future Explorations

**Status:** Deprioritized

The following items are valuable but not core to the positioning. Revisit after Phase 7.

## FederatedConfigurationSource

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

## ConfigurationContext (Application-scoped registry)

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

## APT processor (Java-toolchain-only support)

A `lena-config-apt` module implementing the same generation as `lena-config-ksp`, but built on
plain `javax.annotation.processing` (`Processor`/`RoundEnvironment`), so it can run under a
`javac`-only build with no Kotlin Gradle plugin applied at all.

**Why it's deprioritized:** KSP and `javac`'s APT are two independent, structurally different
SPIs (different symbol models, code-generation backends, incremental-compilation models, and test
harnesses) — dual-maintaining both means two implementations that must be kept behaviorally
identical for every future `@ExternalConfiguration`/`@ConfigurationProperty` feature (Phases 6–7
both extend processor-driven metadata), with real risk of drift and no shared compiler to catch
it. For a learning/experimentation project that has already deprioritized backward compatibility,
that ongoing tax isn't justified without evidence that the KSP-only path (which only requires
consumers to add the Kotlin *compiler* toolchain, not to write Kotlin source) is a hard blocker
for real users. Tracked as [issue #12](https://github.com/riversoforion/lena/issues/12) — react
or comment there to register demand for this feature.

**When to revisit:** If real usage shows the `kotlin("jvm")` + `ksp(...)` toolchain requirement is
a hard blocker for a meaningful segment of pure-Java consumers. Extract the annotation-model-
independent logic (`Name`/type derivation rules, code-generation templates) out of
`lena-config-ksp` into a shared, processor-agnostic layer first, so the APT processor can be a
thin `Processor` adapter over that shared logic rather than a parallel reimplementation.

## Kotlin/WASM and Kotlin/JS targets

Expanding to browser/serverless JavaScript contexts.

**Why it's deprioritized:** These platforms are out of scope for the "desktop-tier" positioning
(JVM, macOS, Linux, Windows native). WASM/JS have fundamentally different configuration patterns
(injected at build time, via environment at runtime, or fetched from a backend API) than what
`lena-config` optimizes for (reading env vars + system properties).

**Alternative:** If Lena users request WASM/JS support, evaluate it then. But the core value prop
(native + JVM) is strong enough without it.
