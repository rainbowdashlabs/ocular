/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.key;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * This class is used to build a configuration key with specific attributes.
 * It provides a way to define the key's name, path, associated class type, and initial value.
 *
 * @param <T> the type of the configuration class the key is associated with
 */
public class KeyBuilder<T> {
    private final Path path;
    private final Class<T> configClazz;
    private final Supplier<T> initValue;
    private String name;

    /**
     * Constructs a new KeyBuilder instance with the specified path and initial value supplier.
     *
     * @param path      the file path associated with the key. The file's name will serve as the key's name.
     * @param initValue a supplier to provide the initial value of type T when the file does not exist.
     *                  The class type of the value is automatically determined from the supplier.
     */
    public KeyBuilder(Path path, Supplier<T> initValue) {
        this.name = path.getFileName().toString();
        this.path = path;
        this.configClazz = (Class<T>) initValue.get().getClass();
        this.initValue = initValue;
    }


    /**
     * Sets the name for the key being built.
     *
     * @param name the name to be assigned to the key
     */
    public void name(String name) {
        this.name = name;
    }

    public Key<T> build() {
        return new Key<>(name, path, configClazz, initValue);
    }
}
