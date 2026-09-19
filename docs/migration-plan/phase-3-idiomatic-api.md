# Phase 3 — Idiomatic Kotlin API redesign

**Status:** Done

- **Drop `ConfigurationPropertiesRegistry`** (global mutable singleton — hostile to native &
  concurrency). Replace nested-config lookup with explicit composition: a parent constructs and
  holds its children directly (e.g. `val network = NetworkConfig(source, namespace.child("net"))`),
  instead of `nested(namespace, Type.class)` reaching into a global map.
- **Property delegates** as the primary Kotlin API on `ConfigurationProperties`:

  ```kotlin
  class AppConfig(source: ConfigurationSource) : ConfigurationProperties(source) {
      val serviceUrl: String by string("service", "url")
      val port: Int by int("port")
      val localMode: Boolean by boolean("local", "mode")
  }
  ```

  Implement `string()/int()/boolean()/long()/double()/short()/float()` as functions returning
  `ReadOnlyProperty<Any?, T>` (or `PropertyDelegateProvider` to capture the property name).
  Keep `stringVal(Name)` etc. as `protected` methods too, for Java subclasses.
- **Defaults & missing-value policy:** replace the `defaultsResolver` Function indirection with a
  clear `MissingValuePolicy` (throw vs null) and per-delegate default support
  (`by string("x") default "y"` or an overload). Preserve `isSet/isDefault/isMissing`.
- **`ConfigurationSources` factory:** Kotlin `object` with `@JvmStatic` `forEnvironment()`,
  `custom(...)`, `prioritized(...)`; `forSystemProperties()` lives in the jvm source set only.
  Add idiomatic Kotlin builders/`vararg` + DSL where it reads well.
- **Java-interop pass:** annotate facades with `@JvmStatic`, default-arg ctors with `@JvmOverloads`,
  and add `@JvmName` where Kotlin names collide with Java expectations. The JVM artifact must stay
  usable from Java (examples are currently Java).
- **Static code analysis:** Integrate [Detekt](https://detekt.dev/) to perform initial and
  ongoing static analysis.
