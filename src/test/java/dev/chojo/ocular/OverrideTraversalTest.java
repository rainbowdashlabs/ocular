/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.traversal.TraversalConfig;
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
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverrideTraversalTest {

    private static final Path BASE = Path.of("traversal_test_config");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
        try (InputStream is = getClass().getResourceAsStream("/traversal_config.json")) {
            Files.copy(is, BASE.resolve("config.json"));
        }
        System.setProperty("nesteddatabase.host", "remotehost");
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("nesteddatabase.host");

        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile).sorted(Comparator.reverseOrder()).forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void jdkTypesOutsideTheRootPackageAreNotTraversed() {
        TraversalConfig config =
                load(Key.builder(Path.of("config.json"), TraversalConfig::new).build());

        assertEquals(Level.WARNING, config.level);
    }

    @Test
    void nestedClassOutsideTheDefaultRootPackageKeepsItsValue() {
        TraversalConfig config =
                load(Key.builder(Path.of("config.json"), TraversalConfig::new).build());

        assertEquals("localhost", config.database.host);
    }

    @Test
    void widenedRootPackageAppliesNestedOverrides() {
        TraversalConfig config = load(Key.builder(Path.of("config.json"), TraversalConfig::new)
                .rootPackage("dev.chojo.classes")
                .build());

        assertEquals("remotehost", config.database.host);
    }

    @Test
    void defaultRootPackageIsThePackageOfTheConfigClass() {
        var key = Key.builder(Path.of("config.json"), TraversalConfig::new).build();

        assertEquals("dev.chojo.classes.traversal", key.rootPackage());
    }

    private TraversalConfig load(Key<TraversalConfig> key) {
        Configurations<TraversalConfig> conf =
                Configurations.builder(key, new JsonDataFormat()).setBase(BASE).build();
        return conf.main();
    }
}
