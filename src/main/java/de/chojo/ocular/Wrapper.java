/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular;

import de.chojo.ocular.key.Key;

import java.io.Closeable;

/**
 * Class allowing to access a configuration file.
 * <p>
 * The file itself will be accessed via the underlying configuration, making it stable for reloads.
 * <p>
 * Using this class inside an auto closable will save the file afterward.
 *
 * @param <T> type of config
 */
public class Wrapper<T> implements Closeable {
    private final Key<T> key;
    private final Configurations<?> config;

    public Wrapper(Key<T> key, Configurations<?> config) {
        this.key = key;
        this.config = config;
    }

    /**
     * Creates a new instance of a {@code Wrapper} for the provided key and configuration.
     *
     * @param <V>    the type of the configuration
     * @param key    the key associated with the configuration
     * @param config the configuration to be wrapped
     * @return a new {@code Wrapper} instance for the given key and configuration
     */
    public static <V> Wrapper<V> of(Key<V> key, Configurations<?> config) {
        return new Wrapper<>(key, config);
    }

    /**
     * Get the wrapped config file.
     *
     * @return config file
     */
    public T config() {
        return config.secondary(key);
    }

    @Override
    public void close() {
        config.save(key);
    }
}
