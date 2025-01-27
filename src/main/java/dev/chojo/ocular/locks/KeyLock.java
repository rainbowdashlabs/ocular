/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.locks;

import dev.chojo.ocular.key.Key;

import java.io.Closeable;

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
