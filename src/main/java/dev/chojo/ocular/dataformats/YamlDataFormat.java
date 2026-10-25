/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import dev.chojo.ocular.exceptions.MissingDataTypeInstallationException;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

/**
 * Reads and writes configuration files as YAML, matching the file endings {@code yaml} and {@code yml}. Requires
 * {@code jackson-dataformat-yaml}.
 */
public class YamlDataFormat implements DataFormat<YAMLMapper, YAMLMapper.Builder> {

    /**
     * Creates a YAML format.
     */
    public YamlDataFormat() {}

    @Override
    public YAMLMapper.Builder createMapper() {
        return YAMLMapper.builder();
    }

    public void configure(YAMLMapper.Builder yaml) {
        yaml.disable(YAMLWriteFeature.USE_NATIVE_TYPE_ID).disable(YAMLWriteFeature.WRITE_DOC_START_MARKER);
    }

    @Override
    public String type() {
        return "yaml";
    }

    @Override
    public String[] typeAlias() {
        return new String[] {"yml"};
    }

    @Override
    public void assertInstalled() throws MissingDataTypeInstallationException {
        DataFormats.requireModule(DataFormats.YAML_MAPPER, type(), DataFormats.YAML_MODULE);
    }
}
