# Phase 6 — Type Coercion & Complex Types (Power)

**Status:** In Progress

Expand value coercion beyond primitives + strings.

## Current Limitation

Today, users work only with primitives + strings:

```kotlin
val timeoutMs: Int by int("timeout")
val timeout = Duration.ofMillis(timeoutMs.toLong())  // manual step

val portsStr: String by string("ports")
val ports = portsStr.split(",").map { it.toInt() }  // manual step
```

## Target API

```kotlin
val timeout: Duration by duration("timeout")
val ports: List<Int> by intList("ports")  // auto-splits on comma
val logLevel: LogLevel by enum("log", "level")
val apiUrl: URL by converted("api", "url") { URL(it) }
```

## Implementation: Type Coercion

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

## Verification: Type Coercion

- `Duration by duration("timeout")` correctly parses ISO-8601 and shorthand
- `List<String> by stringList("hosts")` splits comma-separated values
- Custom converter hook works: `URL by converted("api/url") { URL(it) }`
- Edge cases: empty strings, null, invalid formats throw appropriately

## Cross-Property Defaults

A default value should be able to reference another property's resolved value instead of a
literal — e.g. "default `service.timeout` to whatever `connection.timeout` resolved to."

**The eager technique already works today, with zero framework changes.** `default: T` is a plain
value evaluated at the delegate's/accessor's call site, and properties resolve in Kotlin
declaration order, so an earlier sibling property's *value* is simply usable as a later property's
default:

```kotlin
class NetworkConfig(source: ConfigurationSource, ns: Namespace) : ConfigurationProperties(source, ns) {
    val connectionTimeout: Long by long("connection", "timeout", default = 5_000L)
    // Resolves connectionTimeout's value right here, at construction time.
    val handshakeTimeout: Long by long("handshake", "timeout", default = connectionTimeout)
}
```

Same trick in Java — `default` there is just a method argument:

```java
public long handshakeTimeout() {
    return longVal(Name.of("handshake", "timeout"), connectionTimeout());
}
```

This should be **documented** (README/AGENTS.md `Extending ConfigurationProperties` section) as
the go-to pattern — it needs no new API.

**Add a lazy overload for cases the eager technique can't cover:**

- Referencing a property declared *later* in the same class (declaration order makes this a
  compile error with the eager technique — the sibling isn't initialized yet).
- A default that should reflect the referenced property's value if it could change after
  construction (not a concern for the current design — `ConfigurationSource` reads are already
  effectively point-in-time per access — but worth deciding explicitly rather than assuming).

```kotlin
protected fun long(vararg segments: String, default: () -> Long): ReadOnlyProperty<Any?, Long>
```

Add as a genuine second overload alongside `default: Long` (arity-based overloading, same pattern
used for the Java accessor defaults added in Phase 4) — not a replacement. Most call sites want the
simple eager form; the lambda form is for the forward-reference/deferred case specifically.

**Biggest open design question: scope.** Both techniques above only reach properties on `this` (or
an object already in scope via closure). Real configs are trees of independently-constructed
`ConfigurationProperties` objects (see the `nested-config` example — `ServiceConfig` and the
top-level `net` are siblings, each independently constructed, sharing only a `ConfigurationSource`).
Referencing a value *across* that object boundary (e.g. a child's default falling back to a
value on its parent, or on an unrelated sibling) needs one of:

- **Explicit wiring (works today, no new API):** thread the needed value through the constructor
  as a plain parameter, e.g. `ServiceConfig(source, ns, fallbackTimeout = appConnectionTimeout)`.
  Verbose for deep trees, but requires nothing new and keeps the "no hidden state" design intact.
- **A `default` lambda that closes over a passed-in parent/sibling reference:**
  `default = { parent.connectionTimeout }` — still just explicit constructor wiring, but the
  lambda form makes it read better and defers evaluation. This is likely the sweet spot: no new
  lookup/registry machinery, just the lazy overload above combined with normal closures.
- **A namespace-relative reference resolved through the shared `ConfigurationSource`** (closer to
  Spring's `${other.prop}` string interpolation) — this is a materially bigger feature: it means
  parsing raw source *values* for a reference syntax, resolving that reference against the same
  `ConfigurationSource` the reading property uses, and deciding what happens on cycles or a
  reference to a property that's itself missing/defaulted. Only pursue this if the explicit-wiring
  pattern above proves too awkward in practice — it's a lot of design surface (parsing, cycle
  detection, error reporting) for a use case explicit wiring already covers.

**Recommendation:** ship the lazy `default: () -> T` overload and document both techniques (eager
value, lazy closure over an explicitly-passed reference) as Phase 6 work. Treat the
namespace-relative string-interpolation approach as a *separate*, larger, and lower-priority
feature — revisit only if real usage shows the explicit-wiring pattern is insufficient.
