/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.MyClass;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.exceptions.UnknownFormatException;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParentFormatTest {
    private static final Path BASE = Path.of("configbase-parent");
    private static final Key<MyClass> PARENT_MAIN =
            Key.builder(Path.of("parent.yaml"), () -> new MyClass("Parent", 1)).build();
    private static final Key<MyClass> CHILD_MAIN =
            Key.builder(Path.of("child.json"), () -> new MyClass("Child", 2)).build();
    private static final Key<MyClass> YAML_FILE =
            Key.builder(Path.of("other.yaml"), () -> new MyClass("Lilly", 20)).build();
    private static final Key<MyClass> TOML_FILE =
            Key.builder(Path.of("other.toml"), () -> new MyClass("Lilly", 20)).build();

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile).sorted(Comparator.reverseOrder()).forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void childUsesTheParentFormatForUnknownEndings() {
        Configurations<MyClass> parent = Configurations.builder(PARENT_MAIN, new YamlDataFormat())
                .setBase(BASE)
                .build();
        Configurations<MyClass> child = Configurations.builder(CHILD_MAIN, new JsonDataFormat())
                .setBase(BASE)
                .parent(parent)
                .build();

        MyClass value = child.secondary(YAML_FILE);

        assertEquals("Lilly", value.name());
        assertTrue(Files.exists(BASE.resolve("other.yaml")));
    }

    @Test
    void endingUnknownToChildAndParentFails() {
        Configurations<MyClass> parent = Configurations.builder(PARENT_MAIN, new YamlDataFormat())
                .setBase(BASE)
                .build();
        Configurations<MyClass> child = Configurations.builder(CHILD_MAIN, new JsonDataFormat())
                .setBase(BASE)
                .parent(parent)
                .build();

        assertThrows(UnknownFormatException.class, () -> child.secondary(TOML_FILE));
    }
}
