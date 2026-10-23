/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsGetterVisibilityTest {
    private static final Path BASE = Path.of("configbase-isgetter");
    private static final Key<Settings> MAIN =
            Key.builder(Path.of("settings.json"), Settings::new).build();

    public static class Settings {
        private int players = 10;

        public boolean isFull() {
            return players >= 10;
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile).sorted(Comparator.reverseOrder()).forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void isMethodsAreNotWritten() throws IOException {
        Configurations.builder(MAIN, new JsonDataFormat()).setBase(BASE).build().main();

        String written = Files.readString(BASE.resolve("settings.json"));
        assertTrue(written.contains("players"));
        assertFalse(written.contains("full"));
    }
}
