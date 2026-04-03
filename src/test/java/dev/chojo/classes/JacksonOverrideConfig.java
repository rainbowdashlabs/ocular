/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

public class JacksonOverrideConfig {

    @Overwrite(prop = @Prop("config.host"), env = @Env("CONFIG_HOST"))
    String host;

    @Overwrite(prop = @Prop("config.port"))
    int port;

    @Overwrite(env = @Env("CONFIG_DEBUG"))
    boolean debug;

    private String greeting;

    public JacksonOverrideConfig() {
    }

    @JsonCreator
    public JacksonOverrideConfig(@JsonProperty("host") String host,
                                 @JsonProperty("port") int port,
                                 @JsonProperty("debug") boolean debug,
                                 @JsonProperty("greeting") String greeting) {
        this.host = host;
        this.port = port;
        this.debug = debug;
        this.greeting = greeting;
    }

    public void greeting(String greeting) {
        this.greeting = greeting;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public boolean debug() {
        return debug;
    }

    @Overwrite(prop = @Prop("config.greeting"), env = @Env("CONFIG_GREETING"))
    public String greeting() {
        return greeting;
    }
}
