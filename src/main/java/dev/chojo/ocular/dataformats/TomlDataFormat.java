package dev.chojo.ocular.dataformats;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;

public class TomlDataFormat implements DataFormat<TomlMapper, TomlMapper.Builder> {

    @Override
    public TomlMapper.Builder createMapper() {
        return TomlMapper.builder();
    }

    @Override
    public String type() {
        return "toml";
    }
}
