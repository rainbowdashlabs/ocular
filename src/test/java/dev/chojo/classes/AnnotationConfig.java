/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.EnvVar;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.SysProp;

public class AnnotationConfig {
    // sys checked first, then env overrides (env takes precedence)
    @Overwrite(sys = @SysProp, env = @EnvVar)
    public String test;

    // explicit keys
    @Overwrite(sys = @SysProp("sys.test"), env = @EnvVar("ENV_TEST"))
    public String testPrecise;

    // env checked first, then sys overrides (sys takes precedence)
    @Overwrite(env = @EnvVar("MY_ENV"), sys = @SysProp("my.sys"))
    public String envFirst;

    // multiple sys props: later ones override earlier
    @Overwrite(sys = {@SysProp("primary.prop"), @SysProp("fallback.prop")})
    public String multiSys;

    // multiple env vars: later ones override earlier
    @Overwrite(env = {@EnvVar("PRIMARY_ENV"), @EnvVar("FALLBACK_ENV")})
    public String multiEnv;

    // mixed multiple: sys checked first, then both env vars override
    @Overwrite(sys = @SysProp("base.prop"), env = {@EnvVar("ENV_A"), @EnvVar("ENV_B")})
    public String mixedMultiple;
}
