package dev.chojo.ocular.exceptions;

import dev.chojo.ocular.Format;
import dev.chojo.ocular.Key;

import java.util.Collection;
import java.util.stream.Collectors;

public class UnknownFormatException extends RuntimeException {

    public UnknownFormatException(Key<?> key, Collection<Format<?, ?>> supported) {
        super("No format for %s registered. Supported formats are: %s".formatted(key.path().getFileName().toString(),
                supported.stream().map(format -> format.format().formatString()).collect(Collectors.joining(", "))));
    }
}
