/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import dev.chojo.ocular.exceptions.MissingDataTypeInstallationException;
import tools.jackson.dataformat.toml.TomlMapper;

/**
 * Reads and writes configuration files as TOML. Requires {@code jackson-dataformat-toml}.
 */
public class TomlDataFormat implements DataFormat<TomlMapper, TomlMapper.Builder> {

    /**
     * Creates a TOML format.
     */
    public TomlDataFormat() {}

    @Override
    public TomlMapper.Builder createMapper() {
        return TomlMapper.builder();
    }

    @Override
    public String type() {
        return "toml";
    }

    @Override
    public void assertInstalled() throws MissingDataTypeInstallationException {
        DataFormats.requireModule(DataFormats.TOML_MAPPER, type(), DataFormats.TOML_MODULE);
    }
}
