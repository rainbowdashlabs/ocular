package dev.chojo.ocular;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.chojo.ocular.dataformats.Configurator;
import dev.chojo.ocular.dataformats.DataFormat;
import dev.chojo.ocular.exceptions.ConfigurationException;
import dev.chojo.ocular.exceptions.UnknownFormatException;
import dev.chojo.ocular.hooks.ConfigSubscriber;
import dev.chojo.ocular.locks.KeyLock;
import dev.chojo.ocular.locks.KeyLocks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class Configurations<T> implements Configurator<ObjectMapper, MapperBuilder<ObjectMapper, ?>> {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm");
    private static final Logger log = getLogger(Configurations.class);
    private final Path base;
    private final Key<T> main;
    private final List<Format<?, ?>> formats = new LinkedList<>();
    private final ClassLoader classLoader;
    private final Map<Key<?>, FileWrapper<?>> files = new HashMap<>();
    private final KeyLocks locks = new KeyLocks();

    public Configurations(Path base, @NotNull Key<T> main, List<DataFormat<?, ?>> formats, ClassLoader classLoader) {
        this.base = base;
        this.main = main;
        for (DataFormat<?, ?> format : formats) {
            this.formats.add(new Format<>(format, this));
        }
        this.classLoader = classLoader;
    }

    public static <V> ConfigurationsBuilder<V> builder(Key<V> main, DataFormat<?,?> format){
        return new ConfigurationsBuilder<>(main, format);
    }

    /**
     * Get the primary configuration.
     * <p>
     * This will be the config.yml in most cases.
     * <p>
     * If the config was not yet created, it will be created.
     *
     * @return configuration
     */
    public T main() {
        return secondary(main);
    }

    /**
     * Get a configuration file.
     * <p>
     * If this file was not yet created, it will be created.
     *
     * @param key configuration key
     * @param <V> type of configuration
     * @return configuration file
     */
    @SuppressWarnings("unchecked")
    public synchronized <V> V secondary(Key<V> key) {
        // This configuration might be called to retrieve the logging level.
        // This will cause a recursive call

        if (files.containsKey(key)) {
            return (V) files.get(key).file();
        }

        KeyLock keyLock = locks.tryLock(key);
        if (keyLock == null) {
            // Very rare keys which so far exists for minecraft plugins when the config is created and the log level is read from it.
            return key.initValue().get();
        }

        try (keyLock) {
            FileWrapper<V> v = createAndLoad(key);
            files.put(key, v);
            return v.file();
        }
    }

    /**
     * Load a file defined in the configuration key.
     * <p>
     * Will fail if the file is not present.
     * <p>
     * Use {@link #createAndLoad(Key)} if you want the file to be created.
     *
     * @param key configuration key
     * @param <V> type of file
     * @return instance of file
     */
    protected final <V> FileWrapper<V> load(Key<V> key) {
        if (!exists(key)) return null;
        try {
            return read(determineFormat(key), resolvePath(key), key.configClazz());
        } catch (ConfigurationException e) {
            log.error("Could not load configuration file.", e);
            backup(key);
            log.warn("Recreating default config");
            write(resolvePath(key), new FileWrapper<>(determineFormat(key), key.initValue().get()));
        }
        return new FileWrapper<>(determineFormat(key), key.initValue().get());
    }

    /**
     * Load a file defined in the configuration key.
     * <p>
     * If this file was not yet created, it will be created.
     *
     * @param key configuration key
     * @param <V> type of file
     * @return instance of file
     */
    protected final <V> FileWrapper<V> createAndLoad(Key<V> key) {
        if (!exists(key)) {
            var path = resolvePath(key);
            log.info("Configuration file: {} does not exist. Creating.", path);
            write(path, new FileWrapper<>(determineFormat(key), key.initValue().get()));
        }
        return load(key);
    }

    /**
     * Get the primary configuration wrapper.
     * <p>
     * This wrapper can be used to save the config using the closable.
     * It is also safe to be stored since it does not store the file itself.
     * <p>
     * This will be the config.yml in most cases.
     * <p>
     * If the config was not yet created, it will be created.
     *
     * @return configuration
     */
    public Wrapper<T> mainWrapped() {
        return Wrapper.of(main, this);
    }

    /**
     * Get a configuration file.
     * <p>
     * This wrapper can be used to save the config using the closable.
     * It is also safe to be stored since it does not store the file itself.
     * <p>
     * If this file was not yet created, it will be created.
     *
     * @param key configuration key
     * @param <V> type of configuration
     * @return configuration file
     */
    public <V> Wrapper<V> secondaryWrapped(Key<V> key) {
        return Wrapper.of(key, this);
    }

    /**
     * Checks whether the config file was created already
     *
     * @param key key
     * @param <V> type
     * @return true when exists
     */
    public <V> boolean exists(Key<V> key) {
        return resolvePath(key).toFile().exists();
    }

    /**
     * Checks whether the config file was already loaded
     *
     * @param key key
     * @param <V> type
     * @return true when loaded
     */
    public <V> boolean loaded(Key<V> key) {
        return files.containsKey(key);
    }

    /**
     * Replace the configuration currently associated with this key with a new configuration.
     *
     * @param key      configuration key
     * @param newValue new value of key
     * @param <V>      type of key
     */
    public <V> void replace(Key<V> key, V newValue) {
        files.put(key, new FileWrapper<>(determineFormat(key), newValue));
    }

    public <V> void migrate(Key<V> key, Key<V> newKey) {
        V loaded = secondary(key);
        files.put(newKey, new FileWrapper<>(determineFormat(newKey), loaded));
        save(newKey);
    }

    /**
     * Saves all files loaded via this instance.
     */
    public void save() {
        for (var configKey : files.keySet()) {
            save(configKey);
        }
    }

    /**
     * Saves the file associated with the config key
     *
     * @param key configuration key
     */
    public void save(Key<?> key) {
        write(resolvePath(key), files.get(key));
    }

    /**
     * Reloads all files loaded via this instance including the main configuration.
     */
    public void reload() {
        // We will modify the collection. Therefore, we need to copy first.
        for (var key : new HashSet<>(files.keySet())) {
            reload(key);
        }
    }

    /**
     * Reloads a single file associated with the config key
     *
     * @param key configuration key
     */
    public void reload(Key<?> key) {
        files.put(key, createAndLoad(key));
    }


    @Override
    public void configure(ObjectMapper mapper) {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
              .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        // This is very important when using polymorphism and library loader feature.
        builder.typeFactory(TypeFactory.defaultInstance().withClassLoader(classLoader))
               .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
               .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
               .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }


    public Path base() {
        return base;
    }

    /**
     * Allows to register additional modules to the mapper.
     *
     * @return list of modules.
     */
    protected List<Module> additionalModules() {
        return Collections.emptyList();
    }

    private void backup(Key<?> key) {
        var target = resolvePath(key);
        var backupName = "backup_" + DTF.format(LocalDateTime.now()) + "_" + target.getFileName();
        log.warn("Backing up {} to {}", target, backupName);
        try {
            Files.move(target, target.getParent().resolve(backupName));
            log.error("Backup done.");
        } catch (IOException e) {
            log.error("Could not create backup.");
        }
    }

    private void write(Path path, FileWrapper<?> wrapper) {
        try {
            if (wrapper.file() instanceof ConfigSubscriber sub) {
                sub.preWrite(this);
            }
            Files.createDirectories(path.getParent());
            // We do this to avoid wiping a file on serialization error.
            Files.writeString(path, wrapper.asString());
        } catch (IOException e) {
            log.error("Could not write configuration file to {}", path, e);
            throw new ConfigurationException("Could not write configuration file to " + path, e);
        }
    }

    private <V> FileWrapper<V> read(Format<?, ?> format, Path path, Class<V> clazz) {
        try {
            V v = format.reader().readValue(path.toFile(), clazz);
            if (v instanceof ConfigSubscriber sub) {
                sub.postRead(this);
            }
            return new FileWrapper<>(format, v);
        } catch (IOException e) {
            log.error("Could not read configuration file from {}", path, e);
            throw new ConfigurationException("Could not read configuration file from " + path, e);
        }
    }

    private Path resolvePath(Key<?> key) {
        return key.path().isAbsolute() ? key.path() : base.resolve(key.path());
    }

    private Format<?, ?> determineFormat(Key<?> key) {
        for (var format : formats) {
            if (format.format().matches(key)) {
                return format;
            }
        }
        throw new UnknownFormatException(key, formats);
    }
}
