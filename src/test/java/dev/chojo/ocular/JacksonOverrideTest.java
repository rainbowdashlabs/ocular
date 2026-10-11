/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.JacksonOverrideConfig;
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
import static org.junit.jupiter.api.Assertions.assertFalse;

class JacksonOverrideTest {

    private static final Path BASE = Path.of("jackson_test_config");
    private static final Key<JacksonOverrideConfig> CONFIG_KEY = Key.builder(
            Path.of("config.json"),
            () -> new JacksonOverrideConfig("localhost", 8080, false, "hello")
    ).build();

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
        // Copy the JSON resource into the config base directory so Configurations can load it
        try (InputStream is = getClass().getResourceAsStream("/jackson_override_config.json")) {
            Files.copy(is, BASE.resolve("config.json"));
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("config.host");
        System.clearProperty("config.port");
        System.clearProperty("config.greeting");

        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile)
                      .sorted(Comparator.reverseOrder())
                      .forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void deserializesCorrectlyWithoutOverrides() {
        JacksonOverrideConfig config = loadViaConfigurations();

        assertEquals("localhost", config.host());
        assertEquals(8080, config.port());
        assertFalse(config.debug());
        assertEquals("hello", config.greeting());
    }

    @Test
    void fieldOverrideViaProp() {
        System.setProperty("config.host", "production.example.com");
        System.setProperty("config.port", "9090");

        JacksonOverrideConfig config = loadViaConfigurations();

        assertEquals("production.example.com", config.host());
        assertEquals(9090, config.port());
        // unchanged fields
        assertFalse(config.debug());
        assertEquals("hello", config.greeting());
    }

    @Test
    void methodOverrideViaProp() {
        System.setProperty("config.greeting", "bonjour");

        JacksonOverrideConfig config = loadViaConfigurations();

        assertEquals("bonjour", config.greeting());
        // unchanged fields
        assertEquals("localhost", config.host());
    }

    @Test
    void greetingOverwrittenViaProp() {
        System.setProperty("config.greeting", "overridden-greeting");

        JacksonOverrideConfig config = loadViaConfigurations();

        // The greeting field is private and only accessible via the greeting() method,
        // which is annotated with @Overwrite. Verify the override is applied.
        assertEquals("overridden-greeting", config.greeting());
        // Original values for other fields remain unchanged
        assertEquals("localhost", config.host());
        assertEquals(8080, config.port());
        assertFalse(config.debug());
    }

    @Test
    void fieldAndMethodOverridesCombined() {
        System.setProperty("config.host", "override-host");
        System.setProperty("config.port", "3000");
        System.setProperty("config.greeting", "hola");

        JacksonOverrideConfig config = loadViaConfigurations();

        assertEquals("override-host", config.host());
        assertEquals(3000, config.port());
        assertEquals("hola", config.greeting());
        // debug unchanged (env var only, can't set at runtime)
        assertFalse(config.debug());
    }

    @Test
    void noOverridesPreservesJsonValues() {
        JacksonOverrideConfig config = loadViaConfigurations();

        assertEquals("localhost", config.host());
        assertEquals(8080, config.port());
        assertFalse(config.debug());
        assertEquals("hello", config.greeting());
    }

    private JacksonOverrideConfig loadViaConfigurations() {
        Configurations<JacksonOverrideConfig> conf = Configurations.builder(CONFIG_KEY, new JsonDataFormat())
                                                                   .setBase(BASE)
                                                                   .build();
        return conf.main();
    }
}
