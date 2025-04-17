# Lena Examples

This is a collection of examples demonstrating how to use the Lena libraries. Each subproject is a standalone CLI that illustrates how one or more of the 
libraries can be used in a program. Most of them are small and targeted at a particular library or feature, though some may show how to integrate several 
together.

## Running from Gradle

All the examples expose an `exec` task. They can be executed on the command line with:

```shell
./gradlew examples:<example-project>:exec
```

### Supplying environment variables and system properties

Environment variables can be supplied in the "normal" way for your shell. Any environment variables available to the Gradle process will also become
available to the example program. For instance, on Bash-compatible shells:

```shell
THIS_ENV_VAR="this value" ./gradlew examples:some-example:exec
```

System properties can be supplied in two ways -- system properties (`-D`) or project properties (`P`) to the Gradle command. All system and project
properties will be passed to the example program. The following will make `this.sys.prop` and `this.proj.prop` available to the `some-example` program.

```shell
./gradlew examples:some-example:exec -Dthis.sys.prop="this system property" -Pthis.proj.prop=250
```

## Flag values

When a configuration value is exposed as a boolean flag, the following _case-insensitive_ values can be supplied:

| True   | False   |
|--------|---------|
| `true` | `false` |
| `1`    | `0`     |
| `yes`  | `no`    |
| `y`    | `n`     |
| `on`   | `off`   |

(Strictly speaking, anything other than the "true" values will be interpreted as "false")

## Example Catalog

### Simple Config

- `./simple-config`
- `com.riversoforion.lena.example.SimpleConfigExample`

Demonstrates a very basic usage of `lena-config` to expose environment variables and/or system properties as a type-safe configuration object.
