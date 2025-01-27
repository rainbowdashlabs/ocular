package dev.chojo.ocular;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import dev.chojo.ocular.dataformats.DataFormat;
import dev.chojo.ocular.key.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class CustomConfigurations<T> extends Configurations<T> {
    public CustomConfigurations(Path base, @NotNull Key<T> main, List<DataFormat<?, ?>> formats, ClassLoader classLoader, Configurations<?> parent) {
        super(base, main, formats, classLoader, parent);
    }

    @Override
    public void configure(ObjectMapper mapper) {
        super.configure(mapper); // parent method should be called
    }

    @Override
    public void configure(MapperBuilder<ObjectMapper, ?> builder) {
        super.configure(builder); // Parent method should be called
    }

    @Override
    public void configureWriter(MapperBuilder<ObjectMapper, ?> mapper) {
    }

    @Override
    public void configureWriter(ObjectMapper mapper) {
    }

    @Override
    public void configureReader(MapperBuilder<ObjectMapper, ?> mapper) {
    }

    @Override
    public void configureReader(ObjectMapper mapper) {
    }
}
