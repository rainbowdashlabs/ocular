/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.NestedConfig;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NestedOverrideTest {

    private static final Path BASE = Path.of("nested_test_config");
    private static final Key<NestedConfig> CONFIG_KEY = Key.builder(
            Path.of("config.json"),
            NestedConfig::new
    ).build();

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
        try (InputStream is = getClass().getResourceAsStream("/nested_test_config.json")) {
            Files.copy(is, BASE.resolve("config.json"));
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("nestedconfig.name");
        System.clearProperty("nesteddatabase.host");
        System.clearProperty("nesteddatabase.port");

        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile)
                      .sorted(Comparator.reverseOrder())
                      .forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void nestedFieldOverrideApplied() {
        System.setProperty("nesteddatabase.host", "remotehost");
        System.setProperty("nesteddatabase.port", "3306");

        NestedConfig config = loadViaConfigurations();

        assertEquals("remotehost", config.database.host);
        assertEquals(3306, config.database.port);
    }

    @Test
    void topLevelAndNestedOverridesBothApplied() {
        System.setProperty("nestedconfig.name", "overridden-name");
        System.setProperty("nesteddatabase.host", "remotehost");

        NestedConfig config = loadViaConfigurations();

        assertEquals("overridden-name", config.name);
        assertEquals("remotehost", config.database.host);
    }

    @Test
    void noOverridePreservesNestedValues() {
        NestedConfig config = loadViaConfigurations();

        assertEquals("original", config.name);
        assertEquals("localhost", config.database.host);
        assertEquals(5432, config.database.port);
    }

    private NestedConfig loadViaConfigurations() {
        Configurations<NestedConfig> conf = Configurations.builder(CONFIG_KEY, new JsonDataFormat())
                                                          .setBase(BASE)
                                                          .build();
        return conf.main();
    }
}
