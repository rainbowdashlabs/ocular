# Customise the configurations

There are several ways to configure and customise stuff on the configuration class

## Priorities

There are two levels of configuration.

1. On the format level
2. on the configuration level

Additionally, you can modify only certain parts

1. Writing and reading
2. Only writing
3. Only reading

You can there decide between:

1. Modifying the builder for the ObjectMapper
2. Modifying the ObjectMapper

The order in which the customizations are applied is:

1. **Configurations:** General builder settings
2. **Format:** General builder settings
3. **Configurations:** Writer/Reader builder settings
4. **Format:** Writer/Reader builder settings
5. **Configurations:** General ObjectMapper settings
6. **Format:** General ObjectMapper settings
7. **Configurations:** Writer/Reader ObjectMapper settings
8. **Format:** Writer/Reader ObjectMapper settings

This means that everything on the format level takes precedence over anything that is defined on a global or configurations level

!!! Note

    ObjectMapper is the class of jackson that takes care of serialization and deserialization

## Customise

This means we have a lot of methods to configure everything for each data format. Usually modifying the ObjectMapper is enough, but sometimes you might need to access the builder instead.

During configuration, you interact directly with the jackson ObjectMapper or the builder for such. Some knowledge about jackson is advised here.

### Configurations

Configuration on the global level is easy. Either use the builder or overwrite the methods of the `Configurations` class. Be aware that some values are already set in that class, so you might want to review them.

#### Builder

When using the builder you can use the methods that accept a consumer and start with `configure`.

```java
Configurations<MyClass> conf = Configurations.builder(
                                                     mainConfig,
                                                     new JsonDataFormat())
                                             .addFormat(new YamlDataFormat())
                                             .setBase(Path.of("configurations"))
                                             .configureBuilder()
                                             .configureMapper()
                                             .configureReaderBuilder()
                                             .configureReaderMapper()
                                             .configureWriterBuilder()
                                             .configureWriterMapper()
                                             .build();
```

#### Inheritance

If you want more control or simply move that logic to your own class you can extend the Configurations class directly.

!!! warning

    Some parent functions already contain logic, which should be preserved usually. Have a look at the code of the parent class, to check if it fits your need.

```java
public class CustomConfigurations<T> extends Configurations<T> {
    public CustomConfigurations(Path base, @NotNull Key<T> main, List<DataFormat<?, ?>> formats, ClassLoader classLoader, Configurations<?> parent) {
        super(base, main, formats, classLoader, parent);
    }

    @Override
    public void configure(ObjectMapper mapper) {
        super.configure(mapper); // parent method should be called
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        super.configure(builder); // Parent method should be called
    }

    @Override
    public void configureWriter(MapperBuilder<ObjectMapper, ?> mapper) {
    }

    @Override
    public void configureWriter(ObjectMapper mapper) {
    }

    @Override
    public void configureReader(MapperBuilder<ObjectMapper, ?> mapper) {
    }

    @Override
    public void configureReader(ObjectMapper mapper) {
    }
}
```

## Data Format

The easiest way to customize a format is to extend it. However, customizing a format might not be needed at all usually.

!!! warning

    Some parent functions already contain logic, which should be preserved usually. Have a look at the code of the parent class, to check if it fits your need.


```java
public class CustomJsonFormat extends JsonDataFormat {

    @Override
    public void configure(JsonMapper.Builder mapper) {
    }

    @Override
    public void configureWriter(JsonMapper.Builder mapper) {
    }

    @Override
    public void configureWriter(JsonMapper mapper) {
    }

    @Override
    public void configureReader(JsonMapper.Builder mapper) {
    }

    @Override
    public void configureReader(JsonMapper mapper) {
    }

    @Override
    public void configure(JsonMapper mapper) {
    }
}
```

