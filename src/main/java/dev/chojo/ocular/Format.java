package dev.chojo.ocular;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import dev.chojo.ocular.dataformats.DataFormat;

public class Format<M extends ObjectMapper, B extends MapperBuilder<M, B>> {
    private final DataFormat<M, B> format;
    private final Configurations<?> configurations;
    private ObjectMapper reader;
    private ObjectMapper writer;

    public Format(DataFormat<M, B> format, Configurations<?> configurations) {
        this.format = format;
        this.configurations = configurations;
    }

    public ObjectMapper reader() {
        if (reader == null) {
            B mapper = format.createMapper();
            this.configure(mapper);
            M reader = mapper.build();
            this.configure(reader);
            this.reader = reader;
        }
        return reader;
    }

    public ObjectMapper writer() {
        if (writer == null) {
            B mapper = format.createMapper();
            this.configure(mapper);
            M writer = mapper.build();
            this.configure(writer);
            this.writer = writer;
        }
        return writer;
    }

    private void configure(B mapper) {
        format.configure(mapper);
        configurations.configure((MapperBuilder<ObjectMapper, ?>) mapper);
    }

    private void configure(M mapper) {
        format.configure(mapper);
        configurations.configure(mapper);
        mapper.registerModules(format.additionalModules());
        mapper.registerModules(configurations.additionalModules());
    }

    public DataFormat<?, ?> format() {
        return format;
    }
}
