# Phase 2 — Port `lena-config` core to `commonMain`

**Status:** Done

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
  (see [Phase 4 correctness review](phase-4-tests-examples.md#correctness-review)).
