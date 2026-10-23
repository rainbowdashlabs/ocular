/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.exceptions.ParallelAccessException;
import dev.chojo.ocular.hooks.ConfigSubscriber;
import dev.chojo.ocular.key.Key;
import dev.chojo.ocular.locks.KeyLock;
import dev.chojo.ocular.locks.KeyLocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyLockTest {
    private static final Path BASE = Path.of("configbase-locks");
    private static final Key<SelfReading> SELF =
            Key.builder(Path.of("self.json"), SelfReading::new).build();

    public static class SelfReading implements ConfigSubscriber {
        public String name = "default";
        private transient SelfReading inner;

        @Override
        public void postRead(Configurations<?> config) {
            inner = config.secondary(SELF);
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
    void aLockedKeyCanNotBeLockedAgainUntilClosed() {
        KeyLocks locks = new KeyLocks();
        KeyLock first = locks.tryLock(SELF);
        assertNotNull(first);
        assertNull(locks.tryLock(SELF));
        assertThrows(ParallelAccessException.class, () -> locks.lock(SELF));
        first.close();
        assertNotNull(locks.tryLock(SELF));
    }

    @Test
    void loadingThatAsksForItsOwnKeyGetsTheDefault() {
        SelfReading loaded = Configurations.builder(SELF, new JsonDataFormat())
                .setBase(BASE)
                .build()
                .main();

        assertNotNull(loaded.inner);
        assertEquals("default", loaded.inner.name);
    }
}
