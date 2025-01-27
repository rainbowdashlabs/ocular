/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.dataformats;

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.chojo.ocular.exceptions.MissingDataTypeInstallationException;

public class YamlDataFormat implements DataFormat<YAMLMapper, YAMLMapper.Builder> {

    @Override
    public YAMLMapper.Builder createMapper() {
        return YAMLMapper.builder();
    }

    public void configure(YAMLMapper.Builder yaml) {
        yaml.disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
    }

    @Override
    public String type() {
        return "yaml";
    }

    @Override
    public String[] typeAlias() {
        return new String[]{"yml"};
    }

    @Override
    public void assertInstalled() throws MissingDataTypeInstallationException {
        try {
            Class.forName(YAMLMapper.class.getName());
        } catch (ClassNotFoundException e) {
            throw new MissingDataTypeInstallationException(type(), "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml");
        }
    }
}
