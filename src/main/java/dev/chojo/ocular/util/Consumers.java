package dev.chojo.ocular.util;

import java.util.function.Consumer;

public class Consumers {
    public static <T> Consumer<T> identity() {
        return t -> {
        };
    }
}
