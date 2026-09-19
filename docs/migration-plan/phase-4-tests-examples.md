# Phase 4 — Tests, examples, scaffolding

**Status:** Done

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

## Correctness review

Findings noted during the Phase 1–4 port:

- `Name`/`Namespace` normalization + separator handling — verify round-tripping and the empty/root
  edge cases the tests cover.
- `DefaultValueConverter`: confirm null→0 vs throw-on-invalid behavior is intended and consistent
  across types; the boolean set is asymmetric (no explicit false list) — confirm "anything else =
  false" is desired.
- `ConfigurationProperties` constructor calls `createChildren()` and registry registration during
  construction (open-method-in-constructor smell) — the redesign removes this hazard.
- `prioritized(SortedMap)` ordering semantics — make ordering explicit (insertion order / comparator)
  rather than relying on `SortedMap`.

## Files touched (Phases 1–4)

- Build: `gradle/wrapper/gradle-wrapper.properties`, `gradle/libs.versions.toml`,
  `settings.gradle.kts`, `buildSrc/build.gradle.kts`, `buildSrc/src/main/kotlin/*.gradle.kts`.
- Core (moved Java → `lena-config/src/commonMain/kotlin/org/riversoforion/lena/config/`, with
  `jvmMain`/`nativeMain` actuals): `Name`, `Namespace`, `NameResolver`, `ValueResolver`,
  `ConfigurationSource`, `Simple/Composite/PrioritizedConfigurationSource`, `ConfigurationSources`,
  `ConfigurationProperties`, `ValueConverter`, `DefaultValueConverter`, `resolvers/*`.
- Deleted: `ConfigurationPropertiesRegistry`, old `lena-config-annotation-processor/` tree,
  the three Java convention plugins.
- Reworked `../../AGENTS.md` to describe the KMP layout, delegate API, and expect/actual seams.

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
