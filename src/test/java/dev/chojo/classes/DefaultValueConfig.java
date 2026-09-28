/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

public class DefaultValueConfig {

    @Overwrite(prop = @Prop("default.enabled"), env = @Env("DEFAULT_ENABLED"))
    public boolean enabled = true;

    @Overwrite(prop = @Prop("default.debug"), env = @Env("DEFAULT_DEBUG"))
    public boolean debug = false;

    @Overwrite(prop = @Prop("default.verbose"), env = @Env("DEFAULT_VERBOSE"))
    public boolean verbose = true;

    @Overwrite(prop = @Prop("default.name"), env = @Env("DEFAULT_NAME"))
    public String name = "default-name";

    @Overwrite(prop = @Prop("default.port"), env = @Env("DEFAULT_PORT"))
    public int port = 3000;

    @Overwrite(prop = @Prop("default.ratio"), env = @Env("DEFAULT_RATIO"))
    public double ratio = 0.75;
}
