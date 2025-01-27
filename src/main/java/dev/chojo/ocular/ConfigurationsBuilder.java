/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import dev.chojo.ocular.dataformats.DataFormat;
import dev.chojo.ocular.key.Key;
import dev.chojo.ocular.util.Consumers;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ConfigurationsBuilder is a builder pattern class for creating {@link Configurations} objects.
 * This builder allows users to configure various aspects of a configuration setup, such as the
 * base path, supported data formats, and class loader.
 *
 * @param <T> The type associated with the primary configuration key.
 */
public class ConfigurationsBuilder<T> {
    private Path base = Path.of(".");
    private final @NotNull Key<T> main;
    private final List<DataFormat<?, ?>> formats = new LinkedList<>();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private Configurations<?> parent = null;
    private Consumer<MapperBuilder<ObjectMapper, ?>> configureReaderBuilder = Consumers.identity();
    private Consumer<ObjectMapper> configureReaderMapper = Consumers.identity();
    private Consumer<MapperBuilder<ObjectMapper, ?>> configureWriterBuilder = Consumers.identity();
    private Consumer<ObjectMapper> configureWriterMapper = Consumers.identity();
    private Consumer<MapperBuilder<ObjectMapper, ?>> configureBuilder = Consumers.identity();
    private Consumer<ObjectMapper> configureMapper = Consumers.identity();
    private List<Module> modules = new LinkedList<>();


    /**
     * Constructs a new instance of the ConfigurationsBuilder.
     *
     * @param main   the main configuration key representing the primary file configuration
     * @param format the data format used for managing the file associated with the main key
     */
    public ConfigurationsBuilder(@NotNull Key<T> main, DataFormat<?, ?> format) {
        this.main = main;
        this.formats.add(format);
    }

    /**
     * Sets the base path for configurations.
     *
     * @param base the base path to set
     * @return self
     */
    public ConfigurationsBuilder<T> setBase(Path base) {
        this.base = base;
        return this;
    }

    /**
     * Adds a list of data formats to the builder.
     *
     * @param formats the list of DataFormat objects to be added
     * @return self
     */
    public ConfigurationsBuilder<T> addFormats(List<DataFormat<?, ?>> formats) {
        this.formats.addAll(formats);
        return this;
    }

    /**
     * Adds a single data format to the builder's list of formats.
     *
     * @param format the data format to be added to the builder
     * @return self
     */
    public ConfigurationsBuilder<T> addFormat(DataFormat<?, ?> format) {
        this.formats.add(format);
        return this;
    }

    /**
     * Sets the class loader to be used by this builder.
     *
     * @param classLoader the class loader to be set
     * @return self
     */
    public ConfigurationsBuilder<T> withClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Set a configurations object as parent for the new one. This will essentially make this parent provide the available formats
     *
     * @param parent parent object
     * @return self
     */
    public ConfigurationsBuilder<T> parent(Configurations<?> parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Configures the reader builder with the specified customization logic.
     *
     * @param configureReaderBuilder a {@link Consumer} that accepts a {@link MapperBuilder} for
     *                               customizing the configuration of the reader's {@link ObjectMapper}
     * @return self
     */
    public ConfigurationsBuilder<T> configureReaderBuilder(Consumer<MapperBuilder<ObjectMapper, ?>> configureReaderBuilder) {
        this.configureReaderBuilder = configureReaderBuilder;
        return this;
    }

    /**
     * Configures the {@link ObjectMapper} used for reading data.
     * This method allows the customization of the ObjectMapper instance
     * by applying specific settings or features through the provided consumer.
     *
     * @param configureReaderMapper a consumer to customize the ObjectMapper for reading
     * @return self
     */
    public ConfigurationsBuilder<T> configureReaderMapper(Consumer<ObjectMapper> configureReaderMapper) {
        this.configureReaderMapper = configureReaderMapper;
        return this;
    }

    /**
     * Configures the writer builder for the current configuration. This method accepts a
     * {@link Consumer} that customizes an instance of {@link MapperBuilder} used for creating
     * an {@link ObjectMapper} for writing data.
     *
     * @param configureWriterBuilder a {@link Consumer} that applies custom configurations to the writer builder
     * @return self
     */
    public ConfigurationsBuilder<T> configureWriterBuilder(Consumer<MapperBuilder<ObjectMapper, ?>> configureWriterBuilder) {
        this.configureWriterBuilder = configureWriterBuilder;
        return this;
    }

    /**
     * Configures a writer-specific ObjectMapper instance using the provided customization logic.
     * This allows users to apply additional configurations to the writer ObjectMapper during the
     * setup process.
     *
     * @param configureWriterMapper a Consumer that provides customization logic for the writer ObjectMapper
     * @return self
     */
    public ConfigurationsBuilder<T> configureWriterMapper(Consumer<ObjectMapper> configureWriterMapper) {
        this.configureWriterMapper = configureWriterMapper;
        return this;
    }

    /**
     * Configures the builder used to create and customize the {@link ObjectMapper}.
     * This method accepts a {@link Consumer} that allows detailed customization
     * of the underlying {@link MapperBuilder}.
     *
     * @param configureBuilder a consumer to configure the {@link MapperBuilder}
     * @return self
     */
    public ConfigurationsBuilder<T> configureBuilder(Consumer<MapperBuilder<ObjectMapper, ?>> configureBuilder) {
        this.configureBuilder = configureBuilder;
        return this;
    }

    /**
     * Configures the {@link ObjectMapper} instance using the provided configuration logic.
     *
     * @param configureMapper a {@link Consumer} that accepts an {@link ObjectMapper} and applies
     *                        specific configuration settings to it
     * @return self
     */
    public ConfigurationsBuilder<T> configureMapper(Consumer<ObjectMapper> configureMapper) {
        this.configureMapper = configureMapper;
        return this;
    }

    /**
     * Adds a module to the current configuration builder.
     *
     * @param module the module to be added to the builder
     * @return self
     */
    public ConfigurationsBuilder<T> addModule(Module module) {
        modules.add(module);
        return this;
    }

    /**
     * Adds a collection of modules to the builder's configuration.
     *
     * @param module a collection of modules to be added
     * @return self
     */
    public ConfigurationsBuilder<T> addModule(Collection<Module> module) {
        modules.addAll(module);
        return this;
    }

    public Configurations<T> build() {
        return new ModifyableConfigurations<>(base, main, formats, classLoader, parent, configureReaderBuilder, configureReaderMapper, configureWriterBuilder, configureWriterMapper, configureBuilder, configureMapper, modules);
    }
}
