package dev.chojo.ocular;

import com.fasterxml.jackson.core.JsonProcessingException;

record FileWrapper<T>(Format<?, ?> format, T file) {
    String asString() throws JsonProcessingException {
        return format.writer().writeValueAsString(file);
    }
}
