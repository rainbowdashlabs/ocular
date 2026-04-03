/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.ForcePrefixConfig;
import dev.chojo.classes.PrefixConfig;
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

class OverwritePrefixTest {

    private static final Path PREFIX_BASE = Path.of("prefix_test_config");
    private static final Path FORCE_BASE = Path.of("force_prefix_test_config");

    private static final Key<PrefixConfig> PREFIX_KEY = Key.builder(
            Path.of("config.json"),
            PrefixConfig::new
    ).build();

    private static final Key<ForcePrefixConfig> FORCE_KEY = Key.builder(
            Path.of("config.json"),
            ForcePrefixConfig::new
    ).build();

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(PREFIX_BASE);
        Files.createDirectories(FORCE_BASE);
        try (InputStream is = getClass().getResourceAsStream("/prefix_config.json")) {
            Files.copy(is, PREFIX_BASE.resolve("config.json"));
        }
        try (InputStream is = getClass().getResourceAsStream("/prefix_config.json")) {
            Files.copy(is, FORCE_BASE.resolve("config.json"));
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("myapp.host");
        System.clearProperty("custom.prop");
        System.clearProperty("myapp.custom.prop");

        for (Path base : new Path[]{PREFIX_BASE, FORCE_BASE}) {
            if (Files.exists(base)) {
                try (var walker = Files.walk(base)) {
                    walker.map(Path::toFile)
                          .sorted(Comparator.reverseOrder())
                          .forEachOrdered(File::delete);
                }
            }
        }
    }

    // --- @OverwritePrefix("myapp") without force ---

    @Test
    void prefixUsedForDefaultPropName() {
        // @OverwritePrefix("myapp") + @Overwrite(prop = @Prop) -> myapp.host
        System.setProperty("myapp.host", "from-prefix");

        PrefixConfig target = loadPrefix();

        assertEquals("from-prefix", target.host);
    }

    @Test
    void explicitNameNotPrefixedWithoutForce() {
        // @OverwritePrefix("myapp") + @Overwrite(prop = @Prop("custom.prop")) -> custom.prop (no prefix)
        System.setProperty("custom.prop", "explicit-val");

        PrefixConfig target = loadPrefix();

        assertEquals("explicit-val", target.explicit);
    }

    @Test
    void noOverrideFieldUnchangedWithPrefix() {
        PrefixConfig target = loadPrefix();

        assertNull(target.host);
        assertNull(target.explicit);
    }

    // --- @OverwritePrefix(value = "myapp", force = true) ---

    @Test
    void forcePrefixUsedForDefaultPropName() {
        // force + default name -> myapp.host (same as non-force)
        System.setProperty("myapp.host", "force-default");

        ForcePrefixConfig target = loadForce();

        assertEquals("force-default", target.host);
    }

    @Test
    void forcePrefixPrependedToExplicitProp() {
        // force + @Prop("custom.prop") -> myapp.custom.prop
        System.setProperty("myapp.custom.prop", "force-explicit");

        ForcePrefixConfig target = loadForce();

        assertEquals("force-explicit", target.explicit);
    }

    @Test
    void forcePrefixExplicitNameAloneDoesNotWork() {
        // With force=true, "custom.prop" alone should NOT match (it needs "myapp.custom.prop")
        System.setProperty("custom.prop", "should-not-match");

        ForcePrefixConfig target = loadForce();

        assertNull(target.explicit);
    }

    private PrefixConfig loadPrefix() {
        Configurations<PrefixConfig> conf = Configurations.builder(PREFIX_KEY, new JsonDataFormat())
                                                          .setBase(PREFIX_BASE)
                                                          .build();
        return conf.main();
    }

    private ForcePrefixConfig loadForce() {
        Configurations<ForcePrefixConfig> conf = Configurations.builder(FORCE_KEY, new JsonDataFormat())
                                                               .setBase(FORCE_BASE)
                                                               .build();
        return conf.main();
    }
}
