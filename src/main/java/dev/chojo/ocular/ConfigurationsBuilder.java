package dev.chojo.ocular;

import dev.chojo.ocular.dataformats.DataFormat;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationsBuilder<T> {
    private Path base = Path.of(".");
    private @NotNull Key<T> main;
    private List<DataFormat<?, ?>> formats = new LinkedList<>();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ConfigurationsBuilder(@NotNull Key<T> main, DataFormat<?, ?> format) {
        this.main = main;
        this.formats.add(format);
    }

    public ConfigurationsBuilder<T> setBase(Path base) {
        this.base = base;
        return this;
    }

    public ConfigurationsBuilder<T> addFormats(List<DataFormat<?, ?>> formats) {
        this.formats.addAll(formats);
        return this;
    }
    public ConfigurationsBuilder<T> addFormat(DataFormat<?, ?> format) {
        this.formats.add(format);
        return this;
    }

    public ConfigurationsBuilder<T> withClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public Configurations<T> build() {
        return new Configurations<>(base, main, formats, classLoader);
    }
}
