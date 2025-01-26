/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package dev.chojo.ocular.hooks;

import dev.chojo.ocular.Configurations;

/**
 * This interface may be implemented by plugins in config classes.
 * <p>
 * It provides hooks for read and write operations.
 */
public interface ConfigSubscriber {
    default void postRead(Configurations<?> config) {
    }

    default void preWrite(Configurations<?> config) {
    }
}
