/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.exceptions;

import de.chojo.ocular.Format;
import de.chojo.ocular.key.Key;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This exception signals that an attempt to handle or parse a file failed due to the absence
 * of a registered format for the key specified. The exception provides information about
 * the unsupported key and a list of supported formats.
 */
public class UnknownFormatException extends RuntimeException {

    /**
     * Constructs an {@code UnknownFormatException} with details about the unsupported key and
     * a list of supported formats.
     *
     * @param key       the configuration key that could not be matched to a known format
     * @param supported the collection of supported configuration formats
     */
    public UnknownFormatException(Key<?> key, Collection<Format<?, ?>> supported) {
        super("No format for %s registered. Supported formats are: %s".formatted(key.path().getFileName().toString(),
                supported.stream().map(format -> format.format().formatString()).collect(Collectors.joining(", "))));
    }
}
