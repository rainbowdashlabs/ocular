/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import dev.chojo.ocular.exceptions.MissingDataTypeInstallationException;

/**
 * Creates the data formats Ocular ships, after checking that the Jackson module a format needs is on the class path.
 * <p>
 * A format class can not do this check itself: its method signatures name the classes of the module, so the JVM fails
 * with a {@link NoClassDefFoundError} while loading it, before any of its code runs. This class only names them as
 * text, so a missing module ends in a {@link MissingDataTypeInstallationException} that names the dependency.
 */
public final class DataFormats {
    static final String YAML_MAPPER = "tools.jackson.dataformat.yaml.YAMLMapper";
    static final String YAML_MODULE = "tools.jackson.dataformat:jackson-dataformat-yaml";
    static final String TOML_MAPPER = "tools.jackson.dataformat.toml.TomlMapper";
    static final String TOML_MODULE = "tools.jackson.dataformat:jackson-dataformat-toml";

    private DataFormats() {}

    /**
     * Creates the JSON format without pretty printing.
     *
     * @return the JSON format
     */
    public static JsonDataFormat json() {
        return new JsonDataFormat();
    }

    /**
     * Creates the JSON format.
     *
     * @param prettyPrint whether written files are indented
     * @return the JSON format
     */
    public static JsonDataFormat json(boolean prettyPrint) {
        return new JsonDataFormat(prettyPrint);
    }

    /**
     * Creates the YAML format.
     *
     * @return the YAML format
     * @throws MissingDataTypeInstallationException if {@code jackson-dataformat-yaml} is not on the class path
     */
    public static YamlDataFormat yaml() {
        requireModule(YAML_MAPPER, "yaml", YAML_MODULE);
        return new YamlDataFormat();
    }

    /**
     * Creates the TOML format.
     *
     * @return the TOML format
     * @throws MissingDataTypeInstallationException if {@code jackson-dataformat-toml} is not on the class path
     */
    public static TomlDataFormat toml() {
        requireModule(TOML_MAPPER, "toml", TOML_MODULE);
        return new TomlDataFormat();
    }

    /**
     * Fails unless the class with the given name can be found, without loading it.
     *
     * @param className  the fully qualified name of a class of the module
     * @param dataType   the data type the module is needed for
     * @param dependency the coordinates of the module
     * @throws MissingDataTypeInstallationException if the class can not be found
     */
    static void requireModule(String className, String dataType, String dependency) {
        try {
            Class.forName(className, false, DataFormats.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new MissingDataTypeInstallationException(dataType, dependency);
        }
    }
}
