/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.dataformats;

import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes configuration files as JSON.
 */
public class JsonDataFormat implements DataFormat<JsonMapper, JsonMapper.Builder> {
    private final boolean prettyPrint;

    /**
     * Creates a JSON format.
     *
     * @param prettyPrint {@code true} to write indented JSON
     */
    public JsonDataFormat(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Creates a JSON format that writes compact JSON.
     */
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
}
