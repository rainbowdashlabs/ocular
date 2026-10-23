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

public class KeyLocks {
    private final Set<Key<?>> keys = new HashSet<>();

    public synchronized KeyLock tryLock(Key<?> key) {
        if (!keys.add(key)) {
            return null;
        }
        return new KeyLock(key, this);
    }

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
