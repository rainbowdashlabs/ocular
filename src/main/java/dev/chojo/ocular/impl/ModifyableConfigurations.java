/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.impl;

import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.DataFormat;
import dev.chojo.ocular.key.Key;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Configurations that apply the mapper hooks and modules registered on a {@link dev.chojo.ocular.ConfigurationsBuilder}.
 *
 * @param <T> the type of the main configuration
 */
public class ModifyableConfigurations<T> extends Configurations<T> {
    private final Consumer<MapperBuilder<ObjectMapper, ?>> configureReaderBuilder;
    private final Consumer<ObjectMapper> configureReaderMapper;
    private final Consumer<MapperBuilder<ObjectMapper, ?>> configureWriterBuilder;
    private final Consumer<ObjectMapper> configureWriterMapper;
    private final Consumer<MapperBuilder<ObjectMapper, ?>> configureBuilder;
    private final Consumer<ObjectMapper> configureMapper;
    private final List<JacksonModule> modules;

    /**
     * Creates the configurations.
     *
     * @param base                   the directory relative key paths are resolved against
     * @param main                   the key of the main configuration file
     * @param formats                the data formats available to the keys
     * @param classLoader            the class loader used to resolve configuration types
     * @param parent                 the configurations to fall back to, or {@code null}
     * @param configureReaderBuilder applied to the builder of every reader mapper
     * @param configureReaderMapper  applied to every built reader mapper
     * @param configureWriterBuilder applied to the builder of every writer mapper
     * @param configureWriterMapper  applied to every built writer mapper
     * @param configureBuilder       applied to the builder of every mapper
     * @param configureMapper        applied to every built mapper
     * @param modules                the modules registered on every mapper
     */
    public ModifyableConfigurations(
            Path base,
            @NotNull Key<T> main,
            List<DataFormat<?, ?>> formats,
            ClassLoader classLoader,
            Configurations<?> parent,
            Consumer<MapperBuilder<ObjectMapper, ?>> configureReaderBuilder,
            Consumer<ObjectMapper> configureReaderMapper,
            Consumer<MapperBuilder<ObjectMapper, ?>> configureWriterBuilder,
            Consumer<ObjectMapper> configureWriterMapper,
            Consumer<MapperBuilder<ObjectMapper, ?>> configureBuilder,
            Consumer<ObjectMapper> configureMapper,
            List<JacksonModule> modules) {
        super(base, main, formats, classLoader, parent);
        this.configureReaderBuilder = configureReaderBuilder;
        this.configureReaderMapper = configureReaderMapper;
        this.configureWriterBuilder = configureWriterBuilder;
        this.configureWriterMapper = configureWriterMapper;
        this.configureBuilder = configureBuilder;
        this.configureMapper = configureMapper;
        this.modules = modules;
    }

    @Override
    public List<JacksonModule> additionalModules() {
        if (parent != null) {
            LinkedList<JacksonModule> combinedModules = new LinkedList<>(parent.additionalModules());
            combinedModules.addAll(modules);
            return combinedModules;
        }
        return modules;
    }

    @Override
    public void configureReader(ObjectMapper mapper) {
        configureReaderMapper.accept(mapper);
    }

    @Override
    public void configureReader(MapperBuilder<ObjectMapper, ?> mapper) {
        configureReaderBuilder.accept(mapper);
    }

    @Override
    public void configureWriter(ObjectMapper mapper) {
        configureWriterMapper.accept(mapper);
    }

    @Override
    public void configureWriter(MapperBuilder<ObjectMapper, ?> mapper) {
        configureWriterBuilder.accept(mapper);
    }

    @Override
    public void configure(ObjectMapper mapper) {
        super.configure(mapper);
        configureMapper.accept(mapper);
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        super.configure(builder);
        configureBuilder.accept(builder);
    }
}
