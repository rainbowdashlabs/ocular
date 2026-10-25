/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.locks;

import dev.chojo.ocular.exceptions.ParallelAccessException;
import dev.chojo.ocular.key.Key;

import java.util.HashSet;
import java.util.Set;

/**
 * The keys that are currently locked, so a key is not loaded twice at the same time.
 */
public class KeyLocks {
    private final Set<Key<?>> keys = new HashSet<>();

    /**
     * Creates an empty set of locks.
     */
    public KeyLocks() {}

    /**
     * Locks the key if it is not locked yet.
     *
     * @param key the key to lock
     * @return the lock, or {@code null} if the key is already locked
     */
    public synchronized KeyLock tryLock(Key<?> key) {
        if (!keys.add(key)) {
            return null;
        }
        return new KeyLock(key, this);
    }

    /**
     * Locks the key.
     *
     * @param key the key to lock
     * @return the lock
     * @throws ParallelAccessException if the key is already locked
     */
    public synchronized KeyLock lock(Key<?> key) {
        if (!keys.add(key)) {
            throw new ParallelAccessException();
        }
        return new KeyLock(key, this);
    }

    synchronized void unlock(Key<?> key) {
        keys.remove(key);
    }
}
