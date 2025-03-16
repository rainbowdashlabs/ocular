/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonDataFormat implements DataFormat<JsonMapper, JsonMapper.Builder> {
    private final boolean prettyPrint;

    public JsonDataFormat(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public JsonDataFormat() {
        this(false);
    }

    @Override
    public JsonMapper.Builder createMapper() {
        return JsonMapper.builder();
    }

    @Override
    public String type() {
        return "json";
    }

    @Override
    public void assertInstalled() {
        // json should be always installed
    }

    @Override
    public boolean enablePrettyPrint() {
        return prettyPrint;
    }

    @Override
    public void configure(JsonMapper mapper) {
        if (prettyPrint) {
            mapper.writerWithDefaultPrettyPrinter();
        }
    }
}
