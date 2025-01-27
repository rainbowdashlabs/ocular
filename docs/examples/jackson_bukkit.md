# Integrating jackson bukkit

Jackson bukkit is a [framework](https://github.com/eldoriarpg/jackson-bukkit) that provides mappers for internal minecraft classes to easily dump them into files.

Once you imported the framework (See the github page for that) you can add its module.

Jackson Bukkit provides modules for each server which you can simply add via the builder:

```java
Key<MyClass> mainConfig = Key.builder(Path.of("config.yml"), MyClass::new).build();
Configurations.builder(mainConfig, new YamlDataFormat())
        .withClassLoader(this.getClass().getClassLoader()) // For minecraft its important to pass the classloader
        .addModule(new JacksonPaper())
        .build();
```

Additionally, you should set the classloader to the one of your plugin.
