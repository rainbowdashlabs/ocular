/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.classes.MyClass;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperHookTest {
    private static final Path BASE = Path.of("configbase-hooks");
    private static final Key<MyClass> MAIN =
            Key.builder(Path.of("main.json"), () -> new MyClass("Lilly", 20)).build();

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile).sorted(Comparator.reverseOrder()).forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void everyMapperHookReceivesTheBuiltMapper() {
        List<ObjectMapper> general = new ArrayList<>();
        List<ObjectMapper> reader = new ArrayList<>();
        List<ObjectMapper> writer = new ArrayList<>();
        Configurations<MyClass> configurations = Configurations.builder(MAIN, new JsonDataFormat())
                .setBase(BASE)
                .configureMapper(general::add)
                .configureReaderMapper(reader::add)
                .configureWriterMapper(writer::add)
                .build();

        configurations.main();
        configurations.save();

        assertEquals(1, reader.size());
        assertEquals(1, writer.size());
        assertEquals(2, general.size());
        assertEquals(Set.of(reader.getFirst(), writer.getFirst()), Set.copyOf(general));
    }
}
