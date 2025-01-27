/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.hooks;

import dev.chojo.ocular.Configurations;

/**
 * This interface may be implemented by plugins in config classes.
 * <p>
 * It provides hooks for read and write operations.
 */
public interface ConfigSubscriber {
    /**
     * This method is invoked after the configuration has been read.
     *
     * @param config the configuration object that has been read
     */
    default void postRead(Configurations<?> config) {
    }

    /**
     * A hook that is called before writing configurations. This method can be
     * overridden by implementing classes to perform actions prior to a configuration
     * write operation.
     *
     * @param config the configuration object to be written
     */
    default void preWrite(Configurations<?> config) {
    }
}
