/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular;

import com.fasterxml.jackson.databind.json.JsonMapper;
import de.chojo.ocular.dataformats.JsonDataFormat;

public class CustomJsonFormat extends JsonDataFormat {

    @Override
    public void configure(JsonMapper.Builder mapper) {
    }

    @Override
    public void configureWriter(JsonMapper.Builder mapper) {
    }

    @Override
    public void configureWriter(JsonMapper mapper) {
    }

    @Override
    public void configureReader(JsonMapper.Builder mapper) {
    }

    @Override
    public void configureReader(JsonMapper mapper) {
    }

    @Override
    public void configure(JsonMapper mapper) {
    }
}
