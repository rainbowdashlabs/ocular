/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.key;

import dev.chojo.ocular.Configurations;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * A key for a config file. A config key is considered unique based on the underlying path.
 * <p>
 * Two config keys pointing at the same file, but one being absolute and the other not are not considered equal.
 * However, this should be avoided since it can cause issues when reloading or saving.
 *
 * @param name        name of file
 * @param path        path of file with file ending. Path might be relative to {@link Configurations#base()}
 * @param configClazz class representing the file
 * @param initValue   the initial value when the file does not yet exist.
 * @param strict      whether a file that cannot be read fails loading instead of being backed up and replaced by the
 *                    initial value; use it for files holding user data that must never be reset silently
 * @param <T>         type of file class
 */
public record Key<T>(String name, Path path, Class<T> configClazz, Supplier<T> initValue, boolean strict) {

    /**
     * Creates a key that replaces unreadable files with the initial value after backing them up.
     *
     * @param name        name of file
     * @param path        path of file with file ending
     * @param configClazz class representing the file
     * @param initValue   the initial value when the file does not yet exist
     */
    public Key(String name, Path path, Class<T> configClazz, Supplier<T> initValue) {
        this(name, path, configClazz, initValue, false);
    }

    public static <T> KeyBuilder<T> builder(Path path, Supplier<T> supplier) {
        return new KeyBuilder<>(path, supplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key<?> configKey = (Key<?>) o;

        return path.equals(configKey.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "%s (%s | %s)".formatted(name, path.toString(), configClazz.getSimpleName());
    }

}
