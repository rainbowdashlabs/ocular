/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.MyClass;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataFormatMatchTest {
    private static Key<MyClass> key(String path) {
        return Key.builder(Path.of(path), () -> new MyClass("Lilly", 20)).build();
    }

    @Test
    void matchesTheFileEnding() {
        YamlDataFormat yaml = new YamlDataFormat();
        assertTrue(yaml.matches(key("config.yaml")));
        assertTrue(yaml.matches(key("config.yml")));
        assertTrue(yaml.matches(key("dir/config.yml")));
    }

    @Test
    void ignoresNamesThatOnlyEndWithTheType() {
        YamlDataFormat yaml = new YamlDataFormat();
        assertFalse(yaml.matches(key("fooyaml")));
        assertFalse(yaml.matches(key("config.notyml")));
    }
}
