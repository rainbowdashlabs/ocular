package dev.chojo.ocular;

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
 * @param <T>         type of file class
 */
public record Key<T>(String name, Path path, Class<T> configClazz, Supplier<T> initValue) {
}
