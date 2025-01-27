/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import dev.chojo.ocular.dataformats.DataFormat;

/**
 * The Format class encapsulates the configuration and initialization logic for creating
 * ObjectMapper instances configured for a specific data format. This includes both reader
 * and writer instances, which are lazily initialized and configured according to a specific
 * DataFormat and Configurations provided during construction.
 *
 * @param <M> the type of the specialized ObjectMapper
 * @param <B> the type of the builder used to build the ObjectMapper
 */
public class Format<M extends ObjectMapper, B extends MapperBuilder<M, B>> {
    private final DataFormat<M, B> format;
    private final Configurations<?> configurations;
    private ObjectMapper reader;
    private ObjectMapper writer;

    public Format(DataFormat<M, B> format, Configurations<?> configurations) {
        this.format = format;
        this.configurations = configurations;
    }

    /**
     * Returns a lazily initialized and configured ObjectMapper instance for reading data.
     * If the reader instance is not already created, it initializes it using the associated
     * data format and configurations. Formatting and custom configurations are applied
     * to ensure proper setup before returning the instance.
     *
     * @return the configured ObjectMapper instance for reading
     */
    public ObjectMapper reader() {
        if (reader == null) {
            B mapper = format.createMapper();
            this.configure(mapper);
            configurations.configureReader((MapperBuilder<ObjectMapper, ?>) mapper);
            format.configureReader(mapper);
            M reader = mapper.build();
            this.configure(reader);
            configurations.configureReader(reader);
            format.configureReader(reader);
            this.reader = reader;
        }
        return reader;
    }

    /**
     * Returns a lazily initialized and configured ObjectMapper instance for writing data.
     * If the writer instance is not already created, it initializes it using the associated
     * data format and configurations. Formatting and custom configurations are applied
     * to ensure proper setup before returning the instance.
     *
     * @return the configured ObjectMapper instance for writing
     */
    public ObjectMapper writer() {
        if (writer == null) {
            B mapper = format.createMapper();
            this.configure(mapper);
            configurations.configureWriter((MapperBuilder<ObjectMapper, ?>) mapper);
            format.configureWriter(mapper);
            M writer = mapper.build();
            this.configure(writer);
            configurations.configureWriter((MapperBuilder<ObjectMapper, ?>) mapper);
            format.configureWriter(mapper);
            this.writer = writer;
        }
        return writer;
    }

    /**
     * Configures the given mapper instance using the associated data format and configurations.
     * This method applies specific settings and customizations to the mapper to ensure it is
     * properly initialized and ready for use according to predefined criteria.
     *
     * @param mapper the mapper builder to be configured
     */
    private void configure(B mapper) {
        format.configure(mapper);
        configurations.configure((MapperBuilder<ObjectMapper, ?>) mapper);
    }

    /**
     * Configures the provided mapper instance by applying the settings and modules
     * defined in the associated data format and configurations.
     *
     * @param mapper the mapper instance to configure
     */
    private void configure(M mapper) {
        configurations.configure(mapper);
        format.configure(mapper);
        mapper.registerModules(configurations.additionalModules());
        mapper.registerModules(format.additionalModules());
    }

    /**
     * Retrieves the associated DataFormat instance used for configuring and managing
     * the mapping of data objects. This is the DataFormat instance provided to the
     * containing class during its construction.
     *
     * @return the DataFormat instance encapsulated in this class
     */
    public DataFormat<?, ?> format() {
        return format;
    }
}
