# Configuration Overrides

Ocular supports overriding configuration values at runtime using **environment variables** and **JVM system properties**. This is useful for deploying the same application in different environments (e.g. development, staging, production) without changing configuration files.

## How It Works

The override system has two phases:

1. **Compile time** — An annotation processor scans your configuration classes and generates helper code that knows which environment variables and system properties to read.
2. **Runtime** — After a configuration file is loaded, Ocular automatically checks for override values and applies them to the configuration object.

You annotate fields in your configuration class, and Ocular takes care of the rest.

## Setup

Add the annotation processor to your build so it runs during compilation:

=== "gradle.build.kts"

    ```kts
    dependencies {
        annotationProcessor("dev.chojo", "ocular", "version")
    }
    ```

## Annotating Configuration Fields

Use the `@Overwrite` annotation on any field you want to be overridable. Inside `@Overwrite`, you specify the sources to check:

- `@EnvVar` — read from an environment variable
- `@SysProp` — read from a JVM system property (`-D` flag)

### Basic Example

```java
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.EnvVar;
import dev.chojo.ocular.override.SysProp;

public class AppConfig {
    @Overwrite(env = @EnvVar("APP_HOST"), sys = @SysProp("app.host"))
    private String host = "localhost";

    @Overwrite(env = @EnvVar("APP_PORT"), sys = @SysProp("app.port"))
    private int port = 8080;

    @Overwrite(env = @EnvVar("APP_DEBUG"))
    private boolean debug = false;
}
```

With this configuration:

- Setting the environment variable `APP_HOST=example.com` will override `host` to `"example.com"`.
- Running with `-Dapp.port=9090` will override `port` to `9090`.
- If neither an environment variable nor a system property is set, the original value from the configuration file is kept.

### Automatic Name Derivation

If you don't provide an explicit name, Ocular derives one automatically:

- `@EnvVar()` (no name) → `CLASSNAME_FIELDNAME` in uppercase. For example, field `host` in class `AppConfig` becomes `APPCONFIG_HOST`.
- `@SysProp()` (no name) → `classname.fieldName` in lowercase dot notation. For example, field `host` in class `AppConfig` becomes `appconfig.host`.

```java
public class AppConfig {
    @Overwrite(env = @EnvVar(), sys = @SysProp())
    private String host = "localhost";
    // Checks env var APPCONFIG_HOST and system property appconfig.host
}
```

### Precedence

The order of `env` and `sys` inside `@Overwrite` determines priority. **The first source declared wins** — once a value is found, later sources are ignored.

```java
// System property is checked first, then environment variable.
// If both are set, the system property wins (declared first).
@Overwrite(sys = @SysProp("app.host"), env = @EnvVar("APP_HOST"))
private String host;

// Environment variable is checked first, then system property.
// If both are set, the environment variable wins (declared first).
@Overwrite(env = @EnvVar("APP_HOST"), sys = @SysProp("app.host"))
private String host;
```

## Custom Prefix with `@OverridePrefix`

By default, automatically derived names use the class name as a prefix (e.g. `APPCONFIG_HOST`). You can replace this prefix by annotating the class with `@OverridePrefix`:

```java
import dev.chojo.ocular.override.OverridePrefix;

@OverridePrefix("myapp")
public class AppConfig {
    @Overwrite(env = @EnvVar(), sys = @SysProp())
    private String host = "localhost";
    // Checks env var MYAPP_HOST and system property myapp.host
}
```

When an explicit name is provided in `@EnvVar` or `@SysProp`, the prefix is **not** applied by default:

```java
@OverridePrefix("myapp")
public class AppConfig {
    @Overwrite(env = @EnvVar("CUSTOM_HOST"))
    private String host = "localhost";
    // Checks env var CUSTOM_HOST (not MYAPP_CUSTOM_HOST)
}
```

### Forcing the Prefix

Set `force = true` to always prepend the prefix, even when an explicit name is given:

```java
@OverridePrefix(value = "myapp", force = true)
public class AppConfig {
    @Overwrite(env = @EnvVar("HOST"), sys = @SysProp("port"))
    private String host = "localhost";
    // Checks env var MYAPP_HOST and system property myapp.port
}
```

With `force = true`:

- Env var names become `PREFIX_NAME` (e.g. `MYAPP_HOST`).
- Sys prop names become `prefix.name` (e.g. `myapp.port`).

## Supported Types

The override system supports the following field types. Values from environment variables and system properties (which are always strings) are automatically converted:

- `String`
- `int` / `Integer`
- `long` / `Long`
- `double` / `Double`
- `float` / `Float`
- `boolean` / `Boolean`
- `short` / `Short`
- `byte` / `Byte`

## What Happens Under the Hood

When you compile your project, the annotation processor:

1. Finds every field annotated with `@Overwrite`.
2. For each configuration class, generates a helper class named `<ClassName>_OcularOverride` in the same package.
3. The generated class reads the specified environment variables and system properties and stores any found values in a map.

At runtime, after Ocular reads a configuration file:

1. It looks for the generated `_OcularOverride` class via reflection.
2. If found, it instantiates the class (which reads env vars and system properties).
3. It applies any override values to the matching fields on the configuration object, converting types as needed.

If no override class is found (e.g. no fields were annotated), the configuration is used as-is.
