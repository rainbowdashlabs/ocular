package dev.chojo.ocular.dataformats;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonDataFormat implements DataFormat<JsonMapper, JsonMapper.Builder> {
    @Override
    public JsonMapper.Builder createMapper() {
        return JsonMapper.builder();
    }

    @Override
    public String type() {
        return "json";
    }
}
