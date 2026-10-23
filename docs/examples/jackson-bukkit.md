---
title: Jackson Bukkit
sidebar_position: 1
---

# Jackson Bukkit

[Jackson Bukkit](https://docs.eldoria.de/jackson-bukkit) teaches Jackson how to read and write Bukkit and Paper types
like `Location`, `ItemStack` or `Color`. It is a Jackson module, so you register it on Ocular like any other module.

Add Jackson Bukkit to your plugin as its [documentation](https://docs.eldoria.de/jackson-bukkit) describes. Then pass
the module to the builder:

```java
Key<PluginConfig> main = Key.builder(Path.of("config.yml"), PluginConfig::new).build();

Configurations<PluginConfig> configurations = Configurations.builder(main, new YamlDataFormat())
        .setBase(getDataFolder().toPath())
        .withClassLoader(getClass().getClassLoader())
        .addModule(JacksonPaper.builder().build())
        .build();
```

Use `JacksonBukkit` instead of `JacksonPaper` if your plugin also runs on Spigot.

Set the class loader to the one of your plugin, so Jackson finds your plugin's classes. By default Ocular uses the
context class loader of the current thread.
