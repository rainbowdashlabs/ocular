/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.OverwritePrefix;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

@OverwritePrefix(value = "myapp", force = true)
public class ForcePrefixConfig {
    // Default names: prop -> myapp.host, env -> MYAPP_HOST
    @Overwrite(prop = @Prop, env = @Env)
    public String host;

    // Force mode: explicit names get prefix prepended
    // prop -> myapp.custom.prop, env -> MYAPP_CUSTOM_ENV
    @Overwrite(prop = @Prop("custom.prop"), env = @Env("CUSTOM_ENV"))
    public String explicit;
}
