/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.DefaultValueConfig;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultValueOverrideTest {

    private static final Path BASE = Path.of("default_value_test_config");
    private static final Key<DefaultValueConfig> CONFIG_KEY = Key.builder(
            Path.of("config.json"),
            DefaultValueConfig::new
    ).build();

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("default.enabled");
        System.clearProperty("default.debug");
        System.clearProperty("default.verbose");
        System.clearProperty("default.name");
        System.clearProperty("default.port");
        System.clearProperty("default.ratio");

        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile)
                      .sorted(Comparator.reverseOrder())
                      .forEachOrdered(File::delete);
            }
        }
    }

    private void copyResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/" + resourceName)) {
            Files.copy(is, BASE.resolve("config.json"));
        }
    }

    // --- Field defaults persist when config file has all values but no env/prop is set ---

    @Test
    void configFileValuesOverrideFieldDefaults() throws IOException {
        copyResource("default_value_config.json");

        DefaultValueConfig config = loadViaConfigurations();

        // Config file has enabled=false, overriding field default of true
        assertFalse(config.enabled);
        // Config file has debug=true, overriding field default of false
        assertTrue(config.debug);
        // Config file has verbose=false, overriding field default of true
        assertFalse(config.verbose);
        assertEquals("from-config", config.name);
        assertEquals(9090, config.port);
        assertEquals(1.5, config.ratio);
    }

    // --- Field defaults persist when config file is partial (missing fields) ---

    @Test
    void fieldDefaultsPersistForMissingConfigFields() throws IOException {
        copyResource("default_value_config_partial.json");

        DefaultValueConfig config = loadViaConfigurations();

        // These fields are missing from the partial config, so field defaults should persist
        assertTrue(config.enabled);   // default: true
        assertFalse(config.debug);    // default: false
        assertTrue(config.verbose);   // default: true
        assertEquals(3000, config.port);  // default: 3000
        assertEquals(0.75, config.ratio); // default: 0.75
        // This field IS in the partial config
        assertEquals("partial-config", config.name);
    }

    // --- Boolean field defaults persist when no config file exists (uses init value) ---

    @Test
    void fieldDefaultsPersistWhenConfigCreatedFromScratch() {
        // No config file exists — Configurations will create one from the init value (DefaultValueConfig::new)
        DefaultValueConfig config = loadViaConfigurations();

        assertTrue(config.enabled);   // default: true
        assertFalse(config.debug);    // default: false
        assertTrue(config.verbose);   // default: true
        assertEquals("default-name", config.name);
        assertEquals(3000, config.port);
        assertEquals(0.75, config.ratio);
    }

    // --- Env/prop overrides still work and take precedence ---

    @Test
    void propOverridesFieldDefault() throws IOException {
        copyResource("default_value_config_partial.json");
        System.setProperty("default.enabled", "false");

        DefaultValueConfig config = loadViaConfigurations();

        // Prop override should win over field default
        assertFalse(config.enabled);
    }

    @Test
    void propOverridesConfigFileValue() throws IOException {
        copyResource("default_value_config.json");
        System.setProperty("default.enabled", "true");

        DefaultValueConfig config = loadViaConfigurations();

        // Prop override should win over config file value (which was false)
        assertTrue(config.enabled);
    }

    @Test
    void propOverridesBooleanToTrue() throws IOException {
        copyResource("default_value_config.json");
        System.setProperty("default.debug", "true");

        DefaultValueConfig config = loadViaConfigurations();

        assertTrue(config.debug);
    }

    @Test
    void propOverridesBooleanToFalse() throws IOException {
        copyResource("default_value_config_partial.json");
        System.setProperty("default.verbose", "false");

        DefaultValueConfig config = loadViaConfigurations();

        // Field default is true, but prop override sets it to false
        assertFalse(config.verbose);
    }

    @Test
    void multiplePropsAppliedTogether() throws IOException {
        copyResource("default_value_config_partial.json");
        System.setProperty("default.enabled", "false");
        System.setProperty("default.name", "from-prop");
        System.setProperty("default.port", "5000");

        DefaultValueConfig config = loadViaConfigurations();

        assertFalse(config.enabled);
        assertEquals("from-prop", config.name);
        assertEquals(5000, config.port);
        // Non-overridden fields keep their defaults
        assertTrue(config.verbose);
        assertFalse(config.debug);
    }

    private DefaultValueConfig loadViaConfigurations() {
        Configurations<DefaultValueConfig> conf = Configurations.builder(CONFIG_KEY, new JsonDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        return conf.main();
    }
}
