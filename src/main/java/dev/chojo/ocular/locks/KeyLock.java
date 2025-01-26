package dev.chojo.ocular.locks;

import dev.chojo.ocular.Key;

import java.io.Closeable;
import java.io.IOException;

public class KeyLock implements Closeable {
    private final Key<?> key;
    private final KeyLocks locks;

    KeyLock(Key<?> key, KeyLocks locks) {
        this.key = key;
        this.locks = locks;
    }

    @Override
    public void close() {
        locks.unlock(key);
    }
}
