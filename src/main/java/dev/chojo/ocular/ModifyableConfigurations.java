package dev.chojo.ocular;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import dev.chojo.ocular.dataformats.DataFormat;
import dev.chojo.ocular.key.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class ModifyableConfigurations<T> extends Configurations<T> {
    private final Consumer<MapperBuilder<ObjectMapper, ?>> configureReaderBuilder;
    private final Consumer<ObjectMapper> configureReaderMapper;
    private final Consumer<MapperBuilder<ObjectMapper, ?>> configureWriterBuilder;
    private final Consumer<ObjectMapper> configureWriterMapper;
    private final Consumer<MapperBuilder<ObjectMapper, ?>> configureBuilder;
    private final Consumer<ObjectMapper> configureMapper;
    private final List<Module> modules;


    public ModifyableConfigurations(Path base, @NotNull Key<T> main, List<DataFormat<?, ?>> formats,
                                    ClassLoader classLoader, Configurations<?> parent,
                                    Consumer<MapperBuilder<ObjectMapper, ?>> configureReaderBuilder,
                                    Consumer<ObjectMapper> configureReaderMapper,
                                    Consumer<MapperBuilder<ObjectMapper, ?>> configureWriterBuilder,
                                    Consumer<ObjectMapper> configureWriterMapper,
                                    Consumer<MapperBuilder<ObjectMapper, ?>> configureBuilder,
                                    Consumer<ObjectMapper> configureMapper, List<Module> modules) {
        super(base, main, formats, classLoader, parent);
        this.configureReaderBuilder = configureReaderBuilder;
        this.configureReaderMapper = configureReaderMapper;
        this.configureWriterBuilder = configureWriterBuilder;
        this.configureWriterMapper = configureWriterMapper;
        this.configureBuilder = configureBuilder;
        this.configureMapper = configureMapper;
        this.modules = modules;
    }

    @Override
    public void configureReader(ObjectMapper mapper) {
        configureReaderMapper.accept(mapper);
    }

    @Override
    public void configureReader(MapperBuilder<ObjectMapper, ?> mapper) {
        configureReaderBuilder.accept(mapper);
    }

    @Override
    public void configureWriter(ObjectMapper mapper) {
        configureWriterMapper.accept(mapper);
    }

    @Override
    public void configureWriter(MapperBuilder<ObjectMapper, ?> mapper) {
        configureWriterBuilder.accept(mapper);
    }

    @Override
    public void configure(ObjectMapper mapper) {
        super.configure(mapper);
        configureMapper.accept(mapper);
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        super.configure(builder);
        configureBuilder.accept(builder);
    }
}
