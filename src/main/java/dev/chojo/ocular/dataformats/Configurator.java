package dev.chojo.ocular.dataformats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;

public interface Configurator<M extends ObjectMapper, B extends MapperBuilder<M, ?>> {
    default void configure(B mapper) {
    }

    default void configure(M mapper) {
    }

    default void configureWriter(B mapper) {
    }

    default void configureWriter(M mapper) {
    }

    default void configureReader(B mapper) {
    }

    default void configureReader(M mapper) {
    }
}
