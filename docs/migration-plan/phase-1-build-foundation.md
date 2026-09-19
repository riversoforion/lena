# Phase 1 — Build system foundation

**Status:** Done

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
