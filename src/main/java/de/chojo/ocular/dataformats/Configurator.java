/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.dataformats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;

/**
 * Represents a configurable interface for setting up ObjectMapper
 * or MapperBuilder instances. The Configurator interface provides
 * a set of methods to allow customization and setup of mappers
 * for reading, writing, and general configuration tasks.
 *
 * @param <M> the type of the ObjectMapper
 * @param <B> the type of the MapperBuilder
 */
public interface Configurator<M extends ObjectMapper, B extends MapperBuilder<M, ?>> {
    /**
     * Configures the provided mapper builder with specific settings or customizations.
     *
     * @param mapper the mapper builder instance to be configured
     */
    default void configure(B mapper) {
    }

    /**
     * Configures the given mapper instance by applying custom configurations.
     *
     * @param mapper the mapper instance to be configured
     */
    default void configure(M mapper) {
    }

    /**
     * Configures the provided mapper builder for writing operations.
     *
     * @param mapper the instance of the mapper builder to configure
     */
    default void configureWriter(B mapper) {
    }

    /**
     * Configures the given ObjectMapper instance specifically for writing operations.
     *
     * @param mapper the ObjectMapper instance to be configured for writing
     */
    default void configureWriter(M mapper) {
    }

    /**
     * Configures the provided mapper builder instance for reading operations.
     *
     * @param mapper the mapper builder instance to be configured
     */
    default void configureReader(B mapper) {
    }

    /**
     * Configures the provided ObjectMapper instance for reading.
     *
     * @param mapper the ObjectMapper instance to be configured for reading
     */
    default void configureReader(M mapper) {
    }
}
