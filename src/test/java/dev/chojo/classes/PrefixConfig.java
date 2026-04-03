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

@OverridePrefix("myapp")
public class PrefixConfig {
    // Default names should use "myapp" prefix: sys -> myapp.host, env -> MYAPP_HOST
    @Overwrite(sys = @SysProp, env = @EnvVar)
    public String host;

    // Explicit names should be used as-is (force=false by default)
    @Overwrite(sys = @SysProp("custom.prop"))
    public String explicit;
}
