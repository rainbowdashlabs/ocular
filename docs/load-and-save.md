---
title: Load and save files
sidebar_position: 3
---

# Load and save files

A `Configurations` instance loads each file once and keeps it in memory. You work with plain Java objects and save
them when you are done.

## Read a file

```java
AppConfig config = configurations.main();
Messages messages = configurations.secondary(messagesKey);
```

`main()` returns the file of the main key. `secondary(key)` returns any other file. Both work the same way:

1. If the file is already loaded, you get the object from memory.
2. If the file does not exist, Ocular writes it with the default value of the key.
3. Ocular reads the file, applies [overrides](./overrides.mdx) and keeps the object in memory.

Every call returns the same object until you reload the file.

Two more checks help you decide what to do:

| Method | Returns `true` when |
|---|---|
| `exists(key)` | the file exists on disk |
| `loaded(key)` | the file is loaded in this instance |

## Save

Change the object and save it:

```java
config.port(9090);
configurations.save(mainKey);
```

`save()` without an argument saves every file this instance has loaded.

If a file cannot be written to disk, Ocular throws a `ConfigurationException`.

## Reload

```java
configurations.reload();
configurations.reload(messagesKey);
```

`reload()` reads every loaded file again. `reload(key)` reads one file. A reload creates new objects. Objects you
kept from before stay as they were and are no longer the ones Ocular saves.

### Wrappers

If you want to keep a reference that survives a reload, use a `Wrapper`:

```java
Wrapper<AppConfig> wrapper = configurations.mainWrapped();
Wrapper<Messages> messages = configurations.secondaryWrapped(messagesKey);
```

A wrapper does not hold the object itself. `config()` asks the `Configurations` instance every time, so you always
get the current object. Closing the wrapper saves the file, which works well with try-with-resources:

```java
try (Wrapper<AppConfig> wrapper = configurations.mainWrapped()) {
    wrapper.config().port(9090);
}
```

## Replace and migrate

`replace(key, value)` puts a new object in place of the loaded one. It is not written until you save.

```java
configurations.replace(mainKey, new AppConfig());
configurations.save(mainKey);
```

`migrate(key, newKey)` loads the file of `key` and saves its content under `newKey`. Use it to move a file to a new
name or format. Both keys need the same class.

```java
Key<AppConfig> oldKey = Key.builder(Path.of("config.yaml"), AppConfig::new).build();
Key<AppConfig> newKey = Key.builder(Path.of("config.json"), AppConfig::new).build();

configurations.migrate(oldKey, newKey);
```

The old file stays where it is. Delete it yourself if you no longer need it.

## Broken files

Sometimes a file cannot be read, for example because of a typo. What happens next depends on the key.

By default Ocular repairs the file:

1. It logs the error.
2. It renames the broken file to `backup_<date>_<file name>` in the same directory, for example
   `backup_2026-10-23_03-15_config.yml`.
3. It writes a new file with the default value and returns the default value.

That is fine for settings, but bad for files that hold data your users wrote. Mark those keys as strict:

```java
Key<MobRegistry> mobs = Key.builder(Path.of("mobs.yml"), MobRegistry::new)
        .strict()
        .build();
```

Loading a strict key that cannot be read throws a `ConfigurationException`. The file stays untouched and no backup
is made.

## React to reads and writes

A configuration class can implement `ConfigSubscriber` to hear about reads and writes:

```java
public class AppConfig implements ConfigSubscriber {
    private int port = 8080;

    @Override
    public void postRead(Configurations<?> configurations) {
        if (port < 1) port = 8080;
    }
}
```

| Method | Called |
|---|---|
| `postRead` | after the object was read from the file, before overrides are applied |
| `preWrite` | before the object is written, also when a default file is created |

Both methods have empty defaults, so you only implement what you need.
