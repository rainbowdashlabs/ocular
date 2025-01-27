# Create a Configuration

Everything in Ocular evolves around the Configurations Class. To create it you should use the builder provided by `Configurations.builder`. This builder takes two important inputs.

1. The default configuration file as a `Key`
2. One DataFormat

To understand this we first have to look into two major concepts. The `Keys` and `DataFormats`.

## Key

A `Key` represents a file. This file always needs a path and a default value if it doesn't exist.

Let's create an example key:

```java
Key.builder(Path.of("main.json"), () -> new Serializable("Lilly", 20)).build();
```

This key represents a file called `main.json`. This file will be located directly inside the configuration directory. Its default value is a class with default values. You might also define those directly in the class instead of constructor called inside the supplier.

The builder allows to define a more humanreadable name for the file as well.

!!! Note

    The default value will be recreated again and the default value is not cached. It will also be created once directly to determine the class of the key.

## Data Types

Jackson supports a lot of different data formats. Ocular does not use any default data format on its own. It however provides predefined formats for `json`, `yaml` and `toml`.

If you want to use one of those you need to also import the desired data format with the build tool of your choice and ensure that it is available during runtime. If you go with json you don't need any additional format, since this is supported by jackson already

=== "json"

    ```kts
    implementation("com.fasterxml.jackson.core", "jackson-databind")
    ```

=== "yaml"

    !!! warning "Minecraft Users"
        
        Be aware that jacksons yaml format depends on snakeyaml, which is bundled in spigot and paper. You need to use relocation in that case and shade the complete jackson library instead of using the library builder. You might also encounter version conflicts, when the server software relies on another snakeyaml version than your jackson dataformat.

    ```kts
    implementation("com.fasterxml.jackson.core", "jackson-databind")
    compileOnly("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml")
    ```

=== "toml"

    ```kts
    implementation("com.fasterxml.jackson.core", "jackson-databind")
    compileOnly("com.fasterxml.jackson.dataformat", "jackson-dataformat-toml")
    ```

### Retrieving a data format

There are default data format implementations, you can directly use or inherit them to modify them. Some also provide basic configurations via the constructor.

```java
YamlDataFormat yamlDataFormat = new YamlDataFormat();
JsonDataFormat jsonDataFormat = new JsonDataFormat(true); // Enables pretty printing
TomlDataFormat tomlDataFormat = new TomlDataFormat();
```

### Determining the used dataformat

A configuration can support multiple data formats. The used dataformat for a key is simply determined by the path you define. Each format defines a type and potential alias types that are supported for that format.

If we take a look at the dataformat for yaml we see that every path that ends with `yaml` or `yml` is supported.

```java
public class YamlDataFormat implements DataFormat<YAMLMapper, YAMLMapper.Builder> {

    @Override
    public String type() {
        return "yaml";
    }

    @Override
    public String[] typeAlias() {
        return new String[]{"yml"};
    }
}
```

So make sure your path is well-defined and that the required format is registered for the configuration.

## Creating the configuration instance

For full access to customization you can extend the `Configurations` class. However, the builder of that class allows already a lot of customization.

```java
Key<MyClass> mainConfig = Key.builder(Path.of("config.json"), MyClass::new).build();
Configurations<MyClass> conf = Configurations.builder(mainConfig,
                                                     new JsonDataFormat())
                                             .addFormat(new YamlDataFormat())
                                             .setBase(Path.of("configurations"))
                                             .build();
```

This builder will create a configurations instance that supports the `json` and `yaml` data formats. Additionally, all config files will be created in the `configurations` directory by default.

!!! Note

    If you use an absolute path in your config key, the base will be ignored.
