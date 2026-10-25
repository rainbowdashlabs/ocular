/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.HeldConfig;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HeldOverrideTest {

    private static final Path BASE = Path.of("held_override_test_config");
    private static final Key<HeldConfig> CONFIG_KEY = Key.builder(Path.of("config.json"), HeldConfig::new).build();

    private Configurations<HeldConfig> conf;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
        Files.writeString(BASE.resolve("config.json"), """
                {"token": "file-token", "name": "file-name", "section": {"port": 5432}}
                """);
        System.setProperty("held.token", "secret");
        System.setProperty("held.name", "from-prop");
        System.setProperty("held.port", "9999");
        conf = Configurations.builder(CONFIG_KEY, new JsonDataFormat()).setBase(BASE).build();
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty("held.token");
        System.clearProperty("held.name");
        System.clearProperty("held.port");
        try (var walker = Files.walk(BASE)) {
            walker.map(Path::toFile).sorted(Comparator.reverseOrder()).forEachOrdered(File::delete);
        }
    }

    @Test
    void heldOverrideIsAppliedAndNotWritten() throws IOException {
        assertEquals("secret", conf.main().token);

        conf.save();

        assertEquals("file-token", written().get("token").asString());
        assertEquals("secret", conf.main().token);
    }

    @Test
    void persistedOverrideIsWritten() throws IOException {
        conf.main();

        conf.save();

        assertEquals("from-prop", written().get("name").asString());
    }

    @Test
    void heldOverrideInNestedObjectIsNotWritten() throws IOException {
        assertEquals(9999, conf.main().section.port);

        conf.save();

        assertEquals(5432, written().get("section").get("port").asInt());
        assertEquals(9999, conf.main().section.port);
    }

    @Test
    void valueAssignedByCodeIsWritten() throws IOException {
        conf.main().token = "changed";

        conf.save();

        assertEquals("changed", written().get("token").asString());
    }

    @Test
    void heldOverrideSurvivesReload() throws IOException {
        conf.main();
        conf.save();

        conf.reload();
        conf.save();

        assertEquals("secret", conf.main().token);
        assertEquals("file-token", written().get("token").asString());
    }

    @Test
    void changingAnOverriddenFieldWarnsOnce() {
        conf.main().name = "changed";

        String log = captureErr(() -> {
            conf.save();
            conf.save();
        });

        assertEquals(1, log.split("HeldConfig.name was changed", -1).length - 1);
    }

    @Test
    void unchangedOverriddenFieldDoesNotWarn() {
        conf.main();

        String log = captureErr(conf::save);

        assertFalse(log.contains("was changed"));
    }

    private static String captureErr(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true));
        try {
            action.run();
        } finally {
            System.setErr(original);
        }
        return captured.toString();
    }

    private JsonNode written() throws IOException {
        return new ObjectMapper().readTree(Files.readString(BASE.resolve("config.json")));
    }
}
