/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

public class NestedHolder {
    public static class NestedSettings {
        @Overwrite(prop = @Prop)
        public String value = "default";
    }
}
