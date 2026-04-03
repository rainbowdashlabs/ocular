/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.AnnotationConfig;
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
import static org.junit.jupiter.api.Assertions.assertNull;

class AnnotationOverrideTest {

    private static final Path BASE = Path.of("annotation_test_config");
    private static final Key<AnnotationConfig> CONFIG_KEY = Key.builder(
            Path.of("config.json"),
            AnnotationConfig::new
    ).build();

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
        try (InputStream is = getClass().getResourceAsStream("/annotation_test_config.json")) {
            Files.copy(is, BASE.resolve("config.json"));
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("annotationtest.test");
        System.clearProperty("sys.test");
        System.clearProperty("my.sys");
        System.clearProperty("primary.prop");
        System.clearProperty("fallback.prop");
        System.clearProperty("base.prop");

        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile)
                      .sorted(Comparator.reverseOrder())
                      .forEachOrdered(File::delete);
            }
        }
    }

    private AnnotationConfig loadViaConfigurations() {
        Configurations<AnnotationConfig> conf = Configurations.builder(CONFIG_KEY, new JsonDataFormat())
                                                              .setBase(BASE)
                                                              .build();
        return conf.main();
    }

    @Test
    void defaultSysPropOverridesField() {
        System.setProperty("annotationconfig.test", "from-sys");

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("from-sys", target.test);
    }

    @Test
    void explicitSysPropOverridesField() {
        System.setProperty("sys.test", "precise-value");

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("precise-value", target.testPrecise);
    }

    @Test
    void envFirstSysTakesPrecedence() {
        // In AnnotationTest: @Overwrite(env = @EnvVar("MY_ENV"), sys = @SysProp("my.sys"))
        // env is checked first, then sys overrides — sys takes precedence
        System.setProperty("my.sys", "sys-wins");

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("sys-wins", target.envFirst);
    }

    @Test
    void multiSysLaterOverridesEarlier() {
        // @Overwrite(sys = {@SysProp("primary.prop"), @SysProp("fallback.prop")})
        // Both set: fallback.prop (later) overrides primary.prop
        System.setProperty("primary.prop", "primary");
        System.setProperty("fallback.prop", "fallback");

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("fallback", target.multiSys);
    }

    @Test
    void multiSysOnlyFirstSet() {
        System.setProperty("primary.prop", "only-primary");

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("only-primary", target.multiSys);
    }

    @Test
    void mixedMultipleSysPropApplied() {
        // @Overwrite(sys = @SysProp("base.prop"), env = {@EnvVar("ENV_A"), @EnvVar("ENV_B")})
        // sys checked first, then env overrides — but env vars can't be set at runtime,
        // so only sys prop is applied here
        System.setProperty("base.prop", "base-value");

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("base-value", target.mixedMultiple);
    }

    @Test
    void noPropertySetFieldUnchanged() {
        AnnotationConfig target = loadViaConfigurations();

        assertNull(target.test);
        assertNull(target.testPrecise);
        assertNull(target.envFirst);
        assertNull(target.multiSys);
        assertNull(target.multiEnv);
        assertNull(target.mixedMultiple);
    }

    @Test
    void originalValuesPreservedWhenNoOverrideSet() throws IOException {
        // Replace the null-valued config with one that has actual values
        Files.delete(BASE.resolve("config.json"));
        try (InputStream is = getClass().getResourceAsStream("/annotation_test_config_with_values.json")) {
            Files.copy(is, BASE.resolve("config.json"));
        }

        AnnotationConfig target = loadViaConfigurations();

        assertEquals("original-test", target.test);
        assertEquals("original-precise", target.testPrecise);
        assertEquals("original-envFirst", target.envFirst);
        assertEquals("original-multiSys", target.multiSys);
        assertEquals("original-multiEnv", target.multiEnv);
        assertEquals("original-mixed", target.mixedMultiple);
    }
}
