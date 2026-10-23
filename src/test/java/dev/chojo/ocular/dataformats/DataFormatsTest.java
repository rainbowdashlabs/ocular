/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import dev.chojo.ocular.exceptions.MissingDataTypeInstallationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataFormatsTest {
    @Test
    void createsTheShippedFormats() {
        assertInstanceOf(JsonDataFormat.class, DataFormats.json());
        assertInstanceOf(YamlDataFormat.class, DataFormats.yaml());
        assertInstanceOf(TomlDataFormat.class, DataFormats.toml());
        assertDoesNotThrow(() -> DataFormats.yaml().assertInstalled());
    }

    @Test
    void aMissingModuleNamesTheDependency() {
        MissingDataTypeInstallationException exception = assertThrows(
                MissingDataTypeInstallationException.class,
                () -> DataFormats.requireModule(
                        "tools.jackson.dataformat.missing.Mapper", "missing", "org.example:missing"));
        assertTrue(exception.getMessage().contains("org.example:missing"));
    }
}
