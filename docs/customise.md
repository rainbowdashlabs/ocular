---
title: Customise the mapper
sidebar_position: 4
---

# Customise the mapper

Ocular reads and writes files with a Jackson `ObjectMapper`. Each data format gets two mappers: one for reading and
one for writing. You can change how they are built. Some knowledge of Jackson helps here.

In Jackson 3 a mapper cannot be changed once it is built, so all settings go on its builder.

## What Ocular sets for you

Before your own settings, Ocular sets these on every mapper builder:

- **Fields, not getters.** Jackson sees all fields, including private ones. `get...` methods are ignored.
- **Enums ignore case.** `warn`, `Warn` and `WARN` all read as the constant `WARN`.
- **Your class loader.** Jackson looks up classes with the class loader set through `withClassLoader(...)`.
- **Your modules.** Every module added with `addModule(...)` is registered.

## Use the builder

The quickest way is the `Configurations` builder. Each method takes a consumer that gets the mapper builder:

```java
Configurations<AppConfig> configurations = Configurations.builder(main, new JsonDataFormat())
        .configureBuilder(builder -> builder.enable(SerializationFeature.INDENT_OUTPUT))
        .configureReaderBuilder(builder -> builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
        .configureWriterBuilder(builder -> builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS))
        .addModule(new MyModule())
        .build();
```

| Method | Applies to |
|---|---|
| `configureBuilder` | the reading and the writing mapper |
| `configureReaderBuilder` | only the reading mapper |
| `configureWriterBuilder` | only the writing mapper |

Each method keeps only the last consumer you pass. Put all settings for one hook into one consumer.

The consumers apply to every data format of this instance.

## Extend `Configurations`

If you want to keep the setup in your own class, extend `Configurations` and override its methods. Call
`super.configure(builder)`, since that is where Ocular sets its defaults.

```java
public class MyConfigurations<T> extends Configurations<T> {
    public MyConfigurations(Path base, Key<T> main, List<DataFormat<?, ?>> formats, ClassLoader classLoader) {
        super(base, main, formats, classLoader, null);
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        super.configure(builder);
        builder.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void configureReader(MapperBuilder<ObjectMapper, ?> builder) {
        builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void configureWriter(MapperBuilder<ObjectMapper, ?> builder) {
    }

    @Override
    public List<JacksonModule> additionalModules() {
        return List.of(new MyModule());
    }
}
```

Create it with its constructor instead of `Configurations.builder(...)`:

```java
Configurations<AppConfig> configurations = new MyConfigurations<>(
        Path.of("config"),
        main,
        List.of(new JsonDataFormat(), new YamlDataFormat()),
        getClass().getClassLoader());
```

To change which members Jackson sees, override `visibilityChecker(VisibilityChecker)`.

## Customise a data format

To change only one format, extend it. The format methods get the builder of that format, for example a
`JsonMapper.Builder`:

```java
public class MyJsonFormat extends JsonDataFormat {
    @Override
    public void configure(JsonMapper.Builder builder) {
        builder.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS);
    }

    @Override
    public void configureReader(JsonMapper.Builder builder) {
    }

    @Override
    public void configureWriter(JsonMapper.Builder builder) {
    }
}
```

`YamlDataFormat` already sets things in `configure`. Call `super.configure(builder)` when you override it there.

A format can also bring its own modules with `additionalModules()`, and can claim more file endings with
`typeAlias()`:

```java
public class ConfJsonFormat extends JsonDataFormat {
    @Override
    public String[] typeAlias() {
        return new String[] {"conf"};
    }
}
```

For a completely new format, implement `DataFormat` yourself. You need `createMapper()`, `type()` and
`assertInstalled()`.

## Order of the steps

Each mapper is built in this order. When two steps change the same setting, the later step wins.

1. The format's `configure(builder)`
2. Ocular's defaults, then `configureBuilder` or your `configure(builder)` override
3. Modules: first those of the `Configurations` instance, then those of the format
4. `configureReaderBuilder` or `configureWriterBuilder`, or your `configureReader(builder)` or
   `configureWriter(builder)` override
5. The format's `configureReader(builder)` or `configureWriter(builder)`
6. The mapper is built

So your general settings beat the format's general settings. For the reader and writer steps it is the other way
round: the format comes last.
