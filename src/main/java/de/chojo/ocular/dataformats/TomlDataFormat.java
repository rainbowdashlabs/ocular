/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.dataformats;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import de.chojo.ocular.exceptions.MissingDataTypeInstallationException;

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
        try {
            Class.forName(TomlMapper.class.getName());
        }catch (ClassNotFoundException e){
            throw new MissingDataTypeInstallationException(type(), "com.fasterxml.jackson.dataformat:jackson-dataformat-toml");
        }
    }
}
