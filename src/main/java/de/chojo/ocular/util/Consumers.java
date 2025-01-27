/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.util;

import java.util.function.Consumer;

public class Consumers {
    public static <T> Consumer<T> identity() {
        return t -> {
        };
    }
}
