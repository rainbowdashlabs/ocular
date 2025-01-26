package dev.chojo.ocular.locks;

import dev.chojo.ocular.Key;
import dev.chojo.ocular.exceptions.ParallelAccessException;

import java.util.HashSet;
import java.util.Set;

public class KeyLocks {
    private final Set<Key<?>> keys = new HashSet<>();

    public synchronized KeyLock tryLock(Key<?> key) {
        synchronized (keys) {
            if (keys.contains(key)) {
                return null;
            }
            return new KeyLock(key, this);
        }
    }

    public synchronized KeyLock lock(Key<?> key) {
        synchronized (keys) {
            if (keys.contains(key)) {
                throw new ParallelAccessException();
            }
            return new KeyLock(key, this);
        }
    }

    void unlock(Key<?> key) {
        keys.remove(key);
    }

}
