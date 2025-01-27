package dev.chojo.ocular.dataformats;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import dev.chojo.ocular.Key;

import java.util.Collections;
import java.util.List;

public interface DataFormat<M extends ObjectMapper, B extends MapperBuilder<M, B>> extends Configurator<M, B> {
    B createMapper();

    String type();

    default boolean matches(Key<?> key) {
        if (key.path().toString().endsWith(type())) return true;
        for (String s : typeAlias()) {
            if (key.path().toString().endsWith(s)) return true;
        }
        return false;
    }

    default String[] typeAlias() {
        return new String[0];
    }

    default List<Module> additionalModules() {
        return Collections.emptyList();
    }

    default String formatString() {
        return typeAlias().length == 0 ? type() : "%s (%s)".formatted(type(), String.join(", ", typeAlias()));
    }
}
