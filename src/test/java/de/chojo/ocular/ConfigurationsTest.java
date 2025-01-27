/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular;

import de.eldoria.jacksonbukkit.JacksonPaper;
import de.chojo.classes.MyClass;
import de.chojo.ocular.dataformats.JsonDataFormat;
import de.chojo.ocular.dataformats.TomlDataFormat;
import de.chojo.ocular.dataformats.YamlDataFormat;
import de.chojo.ocular.exceptions.UnknownFormatException;
import de.chojo.ocular.key.Key;
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

    public static final Key<MyClass> JSON = Key.builder(Path.of("main.json"), () -> new MyClass("Lilly", 20)).build();
    public static final Key<MyClass> YAML = Key.builder(Path.of("main.yaml"), () -> new MyClass("Lilly", 20)).build();
    public static final Key<MyClass> YML = Key.builder(Path.of("main.yml"), () -> new MyClass("Lilly", 20)).build();
    public static final Key<MyClass> TOML = Key.builder(Path.of("main.toml"), () -> new MyClass("Lilly", 20)).build();
    public static final Path BASE = Path.of("configbase");


    @BeforeEach
    void setUp() throws IOException {
        YamlDataFormat yamlDataFormat = new YamlDataFormat();
        JsonDataFormat jsonDataFormat = new JsonDataFormat(true); // Enables pretty printing
        TomlDataFormat tomlDataFormat = new TomlDataFormat();
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
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                     .setBase(BASE)
                                                     .build();
        conf.main();
        conf.save();
        Assertions.assertTrue(Files.exists(BASE.resolve(JSON.path())));
    }

    @Test
    void checkSecondaryFileCreation() {
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                     .addFormat(new YamlDataFormat())
                                                     .setBase(BASE)
                                                     .build();
        conf.secondary(YAML);
        conf.save();
        Assertions.assertTrue(Files.exists(BASE.resolve(YAML.path())));
    }

    @Test
    void checkReload() {
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                     .addFormat(new YamlDataFormat())
                                                     .setBase(BASE)
                                                     .build();
        MyClass secondary = conf.secondary(YAML);
        secondary.age(19);
        conf.save();
        conf.reload();
        Assertions.assertEquals(19, conf.secondary(YAML).age());
    }

    @Test
    void loadUnsupportedFormat() {
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                     .addFormat(new YamlDataFormat())
                                                     .setBase(BASE)
                                                     .build();

        Assertions.assertThrows(UnknownFormatException.class, () -> conf.secondary(TOML));
    }

    @Test
    void exists() {
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                     .addFormat(new YamlDataFormat())
                                                     .setBase(BASE)
                                                     .build();
        Assertions.assertFalse(conf.exists(JSON));
        conf.main();
        Assertions.assertTrue(conf.exists(JSON));
    }

    @Test
    void loaded() {
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
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
        Configurations<MyClass> conf = Configurations.builder(JSON, new JsonDataFormat())
                                                     .addFormat(new YamlDataFormat())
                                                     .setBase(BASE)
                                                     .build();
        MyClass yaml = conf.secondary(YAML);
        yaml.age(19);
        conf.migrate(YAML, YML);
        conf.reload();
        MyClass yml = conf.secondary(YML);
        Assertions.assertEquals(19, yml.age());
        Assertions.assertTrue(Files.exists(BASE.resolve(YML.path())));
    }

//    void example() {
//        Key<MyClass> mainConfig = Key.builder(Path.of("config.json"), MyClass::new).build();
//        Configurations<MyClass> conf = Configurations.builder(
//                                                             mainConfig,
//                                                             new JsonDataFormat())
//                                                     .addFormat(new YamlDataFormat())
//                                                     .setBase(Path.of("configurations"))
//                                                     .configureBuilder()
//                                                     .configureMapper()
//                                                     .configureReaderBuilder()
//                                                     .configureReaderMapper()
//                                                     .configureWriterBuilder()
//                                                     .configureWriterMapper()
//                                                     .build();
//    }

    void exampleJacksonBukkit(){
        Key<MyClass> mainConfig = Key.builder(Path.of("config.yml"), MyClass::new).build();
        Configurations.builder(mainConfig, new YamlDataFormat())
                .withClassLoader(this.getClass().getClassLoader()) // For minecraft its important to pass the classloader
                .addModule(new JacksonPaper())
                .build();
    }
}
