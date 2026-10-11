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

@OverwritePrefix("myapp")
public class PrefixConfig {
    // Default names should use "myapp" prefix: prop-> myapp.host, env -> MYAPP_HOST
    @Overwrite(prop = @Prop, env = @Env)
    public String host;

    // Explicit names should be used as-is (force=false by default)
    @Overwrite(prop = @Prop("custom.prop"))
    public String explicit;
}
