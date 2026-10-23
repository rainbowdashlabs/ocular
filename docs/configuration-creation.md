---
title: Create a configuration
sidebar_position: 2
---

# Create a configuration

Everything in Ocular starts with the `Configurations` class. You create it with `Configurations.builder(...)`, which
takes two things:

1. a `Key` for your main configuration file,
2. a `DataFormat` for that file.

## Keys

A `Key` describes one file. It knows the path of the file and how to create its default value.

```java
Key<AppConfig> key = Key.builder(Path.of("config.yml"), AppConfig::new).build();
```

This key stands for the file `config.yml` inside the base directory of your configuration. The supplier creates the
default value. Ocular calls it whenever it needs a fresh default, for example when the file does not exist yet. The
value is not cached.

The builder also calls the supplier once right away. It uses the result to find out the class of your
configuration. So the supplier must work at that point.

The class must be something Jackson can create, for example a class with a no-argument constructor.

### Key options

| Method | What it does | Default |
|---|---|---|
| `name(String)` | A readable name for the file. It shows up in `toString()`. | The file name of the path |
| `strict()` | Loading fails when the file cannot be read. The file stays as it is. See [broken files](./load-and-save.md#broken-files). | Not strict |
| `rootPackage(String)` | The package of your configuration classes, used for [nested overrides](./overrides.mdx#nested-objects). | The package of the configuration class |

Two keys are equal when their paths are equal. A relative and an absolute path to the same file count as different
keys, so stick to one style.

### Paths

A relative path is resolved against the base directory of the `Configurations` instance. An absolute path is used
as it is and ignores the base directory.

Sub directories are fine. Ocular creates missing directories when it writes a file.

```java
Key<Messages> messages = Key.builder(Path.of("lang", "messages.yml"), Messages::new).build();
```

## Data formats

Ocular ships three data formats:

| Class | Format | File endings | Extra dependency |
|---|---|---|---|
| `JsonDataFormat` | JSON | `json` | none |
| `YamlDataFormat` | YAML | `yaml`, `yml` | `tools.jackson.dataformat:jackson-dataformat-yaml` |
| `TomlDataFormat` | TOML | `toml` | `tools.jackson.dataformat:jackson-dataformat-toml` |

See [YAML and TOML](./index.mdx#yaml-and-toml) for how to add the extra dependencies.

```java
JsonDataFormat json = new JsonDataFormat();
JsonDataFormat prettyJson = new JsonDataFormat(true);
YamlDataFormat yaml = new YamlDataFormat();
TomlDataFormat toml = new TomlDataFormat();
```

`JsonDataFormat` writes everything on one line by default. Pass `true` to the constructor to get indented output.

```json
{
  "host" : "localhost",
  "port" : 8080
}
```

`YamlDataFormat` writes files without the `---` start marker and without native YAML type ids.

### Which format reads a file

Ocular picks the format by the end of the file path. Each format has a type and can have aliases. YAML has the type
`yaml` and the alias `yml`, so both `config.yaml` and `config.yml` use it.

When several formats match, the first registered one wins. When none matches, Ocular throws an
`UnknownFormatException` that lists the formats you registered:

```text
No format for config.xml registered. Supported formats are: json, yaml (yml)
```

Make sure every file ending you use has a registered format.

## Build the configuration

```java
Key<AppConfig> main = Key.builder(Path.of("config.json"), AppConfig::new).build();

Configurations<AppConfig> configurations = Configurations.builder(main, new JsonDataFormat())
        .addFormat(new YamlDataFormat())
        .setBase(Path.of("configurations"))
        .build();
```

This instance reads and writes JSON and YAML files. All relative paths live in the `configurations` directory.

### Builder options

| Method | What it does | Default |
|---|---|---|
| `setBase(Path)` | The directory that relative key paths are resolved against. | `Path.of(".")`, the working directory |
| `addFormat(DataFormat)` | Registers one more data format. | Only the format passed to `builder(...)` |
| `addFormats(List<DataFormat<?, ?>>)` | Registers several data formats at once. | |
| `withClassLoader(ClassLoader)` | The class loader Jackson uses to find classes. | The context class loader of the current thread |
| `addModule(JacksonModule)` | Registers a Jackson module on every mapper. There is also a version that takes a collection. | No modules |
| `configureBuilder(...)` | Changes the Jackson mapper builder. | |
| `configureReaderBuilder(...)` | Changes the mapper builder for reading only. | |
| `configureWriterBuilder(...)` | Changes the mapper builder for writing only. | |

The `configure...` methods are explained in [Customise the mapper](./customise.md).

If the Jackson module of a format is missing at runtime, Ocular fails as soon as you create or register that
format.

The main key is just the first file. You can load as many other files as you like through the same instance. See
[Load and save files](./load-and-save.md).
