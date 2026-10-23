/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import dev.chojo.ocular.exceptions.MissingDataTypeInstallationException;
import tools.jackson.dataformat.toml.TomlMapper;

public class TomlDataFormat implements DataFormat<TomlMapper, TomlMapper.Builder> {

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
