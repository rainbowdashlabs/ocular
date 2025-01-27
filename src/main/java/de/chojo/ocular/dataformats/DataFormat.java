/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.dataformats;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import de.chojo.ocular.exceptions.MissingDataTypeInstallationException;
import de.chojo.ocular.key.Key;

import java.util.Collections;
import java.util.List;

/**
 * Interface representing a data format used for configuring and initializing Jackson ObjectMapper
 * instances with specific serialization and deserialization capabilities.
 *
 * @param <M> the type of ObjectMapper associated with the data format
 * @param <B> the type of MapperBuilder used to configure the associated ObjectMapper
 */
public interface DataFormat<M extends ObjectMapper, B extends MapperBuilder<M, B>> extends Configurator<M, B> {
    /**
     * Creates and returns a new instance of the mapper builder associated with the data format.
     * This method is used to initialize the builder for configuring the ObjectMapper for
     * reading and writing operations in the desired format.
     *
     * @return a new instance of the mapper builder for the current data format
     */
    B createMapper();

    /**
     * Returns the primary type identifier for the data format.
     * The type is used to uniquely identify the data format and may be used
     * for configuration, validation, or matching purposes.
     *
     * @return the primary type identifier for the data format as a non-null string
     */
    String type();

    /**
     * Checks whether the specified key matches the type or any of the type aliases of the current data format.
     *
     * @param key the key to be checked, including its underlying path.
     * @return true if the path of the specified key ends with the type or any of its aliases; false otherwise.
     */
    default boolean matches(Key<?> key) {
        String path = key.path().toString();
        if (path.endsWith(type())) return true;
        for (String s : typeAlias()) {
            if (path.endsWith(s)) return true;
        }
        return false;
    }

    /**
     * Ensures that the necessary module for handling the specific data format is installed and available.
     * This method checks whether the required library or configuration for the data format is present.
     * If the required module is not installed, a {@link MissingDataTypeInstallationException} will be thrown.
     *
     * @throws MissingDataTypeInstallationException if the required module for the data format
     *         is not installed or available in the classpath.
     */
    void assertInstalled() throws MissingDataTypeInstallationException;

    /**
     * Provides a list of alternative type aliases for the data format.
     *
     * @return an array of strings containing aliases for the primary type of the data format.
     *         Defaults to an empty array if no aliases are defined.
     */
    default String[] typeAlias() {
        return new String[0];
    }

    /**
     * Provides additional Jackson modules required by the implementing data format.
     * This method allows for registering supplementary modules to enhance the functionality
     * of the Jackson ObjectMapper, depending on the specific requirements of the data format.
     *
     * @return a list of additional Jackson modules to be registered; returns an empty list
     *         if no additional modules are needed
     */
    default List<Module> additionalModules() {
        return Collections.emptyList();
    }

    /**
     * Formats the type information of this data format instance into a readable string.
     * If no type aliases are available, it returns the primary type. Otherwise, it
     * combines the primary type with its aliases in parentheses.
     *
     * @return a formatted string representing the primary type with its aliases if available.
     */
    default String formatString() {
        return typeAlias().length == 0 ? type() : "%s (%s)".formatted(type(), String.join(", ", typeAlias()));
    }
}
