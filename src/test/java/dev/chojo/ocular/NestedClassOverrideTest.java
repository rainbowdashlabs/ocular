/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.NestedHolder;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NestedClassOverrideTest {
    private static final Path BASE = Path.of("configbase-nested-class");
    private static final Key<NestedHolder.NestedSettings> MAIN = Key.builder(
                    Path.of("settings.json"), NestedHolder.NestedSettings::new)
            .build();

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("nestedsettings.value");
        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile).sorted(Comparator.reverseOrder()).forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void overridesWorkInNestedClasses() {
        System.setProperty("nestedsettings.value", "from-sys");

        NestedHolder.NestedSettings settings = Configurations.builder(MAIN, new JsonDataFormat())
                .setBase(BASE)
                .build()
                .main();

        assertEquals("from-sys", settings.value);
    }
}
