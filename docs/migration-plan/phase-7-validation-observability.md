# Phase 7 — Validation & Observability (Quality)

**Status:** Planned

Add runtime guardrails and visibility into configuration decisions.

## Validation DSL

```kotlin
class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
    val port: Int by int("port") validate { it in 1..65535 }
    val timeout: Duration by duration("timeout") validate { it.seconds > 0 }
    val name: String by string("app", "name") validate { it.isNotBlank() }
}
```

Validation failures throw on property access (lazy, not at construction).

## Implementation: Validation

1. Add `validate { ... }` extension on delegates (returns a wrapper delegate that intercepts access)
2. Collect validation errors and expose via:
   - `isValid(): Boolean`
   - `validationErrors(): List<ValidationError>`
3. Support for JSR-380 (Jakarta Bean Validation) annotations on generated interfaces
   (via KSP Phase 5 processor — `@Min(1)`, `@Max(65535)`, etc.)

## Restoring `isDefault` — Design Tension

The pre-Kotlin design had `isDefault(Name): Boolean`, backed by a `createDefaults(): Map<Name, String>`
registered up front — so the answer was available for *any* `Name` at *any* time, whether or not
the property had been read yet. The Phase 3 redesign deliberately dropped the up-front defaults
map (it was map-based indirection the property-delegate redesign was explicitly moving away from),
and with it went `isDefault`. `isMissing`/`isSet` survived because they only need a raw
`source.getValue(namespace, name) == null` check — no registered state required.

**Rejected approach: track outcome at access time.** The obvious-seeming fix is to have each
delegate/accessor record `SET` / `DEFAULT` / `MISSING` into an internal `Map<Name, Outcome>` the
moment it's read, and have `isDefault(name)` consult that map. This was the first idea floated for
this phase, but it has a real semantic gap versus the old behavior: `isDefault(name)` can only
answer for properties that have *already been read* through their delegate/accessor at least once.
Ask about an unread property and there's nothing to report — silently wrong (returns `false` for a
property that data *would* default if read) or requires throwing/returning `Optional`, either of
which is worse than the thing being replaced. This is why the design isn't settled yet — flagging
it explicitly here rather than shipping the first idea that compiles.

**Better approach: register at declaration time, decide at query time.** The two concerns —
"was a default declared for this `Name`" and "is the source currently missing" — don't need to be
answered by the same mechanism, and only the second one is genuinely dynamic:

- Every delegate/accessor overload that takes a `default` argument already runs *eagerly*, in
  declaration order, at construction time (see [Phase 6's cross-property-defaults
  section](phase-6-type-coercion.md#cross-property-defaults) — this is the same eagerness that
  already makes `default = connectionTimeout` work). That same eager call can register `name` into
  an internal `MutableSet<Name>` (or `Map<Name, Any?>`, if the default value itself is worth
  exposing for introspection/doc generation) — no reflection, no hidden global state, purely a side
  effect of construction that's already happening.
- `isDefault(name)` then becomes: `isMissing(name) && name in declaredDefaults`. No per-access
  tracking, no "unread property" gap — it's answerable immediately after construction, exactly
  like the old registered-defaults-map behavior, without reintroducing a pre-registered map of
  *values* (only of *which names have a default*, which the delegate/accessor call already knows).

This composes cleanly with the Observability Hooks below — `onPropertyRead` can report the same
`SET` / `DEFAULT` / `MISSING` classification computed via `isDefault`/`isMissing` at read time,
rather than needing its own separate tracking.

**Open question for implementation time:** should `declaredDefaults` also store the default
*value* (enabling `defaultValue(name): Any?` for doc/schema generation via the Phase 5 KSP
processor), or just membership? Leaning toward storing the value — it's nearly free once you're
already registering the name, and directly useful for the property-metadata/doc-generation use
case Phase 5 already wants.

## Observability Hooks

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

## Implementation: Observability

1. Add optional `onPropertyRead`, `onPropertyMissing`, `onValidationError` callbacks to
   `ConfigurationProperties`
2. Invoke callbacks at appropriate points (in delegates, in validators)
3. Provide a built-in logger callback for convenience
4. Expose property metadata via Phase 5 processor for static analysis (which properties should
   exist, which are required, which have defaults)

## Verification: Validation & Observability

- Validation error is thrown on property access if constraint violated
- Observability callbacks fire with correct name/value/source
- Integration test: configure a mock observer, assert expected properties are read
