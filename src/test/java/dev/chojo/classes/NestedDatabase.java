/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

public class NestedDatabase {
    @Overwrite(prop = @Prop, env = @Env)
    public String host = "localhost";

    @Overwrite(prop = @Prop, env = @Env)
    public int port = 5432;
}
