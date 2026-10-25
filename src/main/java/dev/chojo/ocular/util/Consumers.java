/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.util;

import java.util.function.Consumer;

/**
 * Helpers for {@link Consumer consumers}.
 */
public class Consumers {
    private Consumers() {}

    /**
     * Returns a consumer that does nothing.
     *
     * @param <T> the type of the consumed value
     * @return a consumer that ignores its input
     */
    public static <T> Consumer<T> identity() {
        return t -> {};
    }
}
