package dev.chojo.ocular;

import dev.chojo.classes.SerializableRecord;
import dev.chojo.ocular.dataformats.JsonDataFormat;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.exceptions.UnknownFormatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class ConfigurationsTest {

    public static final Key<SerializableRecord> JSON = Key.of("main", Path.of("main.json"), SerializableRecord.class, () -> new SerializableRecord("Lilly", 20));
    public static final Key<SerializableRecord> YAML = Key.of("main", Path.of("main.yaml"), SerializableRecord.class, () -> new SerializableRecord("Lilly", 20));
    public static final Key<SerializableRecord> YML = Key.of("main", Path.of("main.yml"), SerializableRecord.class, () -> new SerializableRecord("Lilly", 20));
    public static final Key<SerializableRecord> TOML = Key.of("main", Path.of("main.toml"), SerializableRecord.class, () -> new SerializableRecord("Lilly", 20));
    public static final Path BASE = Path.of("configbase");


    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(BASE)) {
            try (var walker = Files.walk(BASE)) {
                walker.map(Path::toFile)
                        .sorted(Comparator.reverseOrder())
                        .forEachOrdered(File::delete);
            }
        }
    }

    @Test
    void checkMainFileCreation() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        conf.main();
        conf.save();
        Assertions.assertTrue(Files.exists(BASE.resolve(JSON.path())));
    }

    @Test
    void checkSecondaryFileCreation() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .addFormat(new YamlDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        conf.secondary(YAML);
        conf.save();
        Assertions.assertTrue(Files.exists(BASE.resolve(YAML.path())));
    }

    @Test
    void checkReload() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .addFormat(new YamlDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        SerializableRecord secondary = conf.secondary(YAML);
        secondary.age(19);
        conf.save();
        conf.reload();
        Assertions.assertEquals(19, conf.secondary(YAML).age());
    }

    @Test
    void loadUnsupportedFormat() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .addFormat(new YamlDataFormat())
                                                                .setBase(BASE)
                                                                .build();

        Assertions.assertThrows(UnknownFormatException.class, () -> conf.secondary(TOML));
    }

    @Test
    void exists() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .addFormat(new YamlDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        Assertions.assertFalse(conf.exists(JSON));
        conf.main();
        Assertions.assertTrue(conf.exists(JSON));
    }

    @Test
    void loaded() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .addFormat(new YamlDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        conf.main();
        conf = Configurations.builder(JSON, new JsonDataFormat())
                             .addFormat(new YamlDataFormat())
                             .setBase(BASE)
                             .build();
        Assertions.assertTrue(conf.exists(JSON));
        Assertions.assertFalse(conf.loaded(JSON));
        conf.main();
        Assertions.assertTrue(conf.loaded(JSON));
    }

    @Test
    void migrate() {
        Configurations<SerializableRecord> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                                .addFormat(new YamlDataFormat())
                                                                .setBase(BASE)
                                                                .build();
        SerializableRecord yaml = conf.secondary(YAML);
        yaml.age(19);
        conf.migrate(YAML, YML);
        conf.reload();
        SerializableRecord yml = conf.secondary(YML);
        Assertions.assertEquals(19, yml.age());
        Assertions.assertTrue(Files.exists(BASE.resolve(YML.path())));
    }
}
