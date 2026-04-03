/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.EnvVar;
import dev.chojo.ocular.override.OverridePrefix;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.SysProp;

@OverridePrefix(value = "myapp", force = true)
public class ForcePrefixConfig {
    // Default names: sys -> myapp.host, env -> MYAPP_HOST
    @Overwrite(sys = @SysProp, env = @EnvVar)
    public String host;

    // Force mode: explicit names get prefix prepended
    // sys -> myapp.custom.prop, env -> MYAPP_CUSTOM_ENV
    @Overwrite(sys = @SysProp("custom.prop"), env = @EnvVar("CUSTOM_ENV"))
    public String explicit;
}
