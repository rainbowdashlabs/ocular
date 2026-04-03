/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

public class AnnotationConfig {
    // prop is declared first, so it takes precedence over env
    @Overwrite(prop = @Prop, env = @Env)
    public String test;

    // explicit keys
    @Overwrite(prop = @Prop("sys.test"), env = @Env("ENV_TEST"))
    public String testPrecise;

    // env is declared first, so it takes precedence over prop
    @Overwrite(env = @Env("MY_ENV"), prop = @Prop("my.sys"))
    public String envFirst;

    // multiple props: first one with a value wins
    @Overwrite(prop = {@Prop("primary.prop"), @Prop("fallback.prop")})
    public String multiSys;

    // multiple env vars: first one with a value wins
    @Overwrite(env = {@Env("PRIMARY_ENV"), @Env("FALLBACK_ENV")})
    public String multiEnv;

    // mixed multiple: prop is declared first, so it takes precedence; env vars are fallbacks
    @Overwrite(prop = @Prop("base.prop"), env = {@Env("ENV_A"), @Env("ENV_B")})
    public String mixedMultiple;
}
