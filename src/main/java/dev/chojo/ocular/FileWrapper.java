/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A generic wrapper class that associates a file object with a specific data format
 * encapsulated by the {@link Format} class. This class provides a utility method
 * to serialize the encapsulated file object into a string representation using
 * the configured writer of the associated format.
 *
 * @param <T> the type of the file object being wrapped
 */
record FileWrapper<T>(Format<?, ?> format, T file) {
    /**
     * Serializes the encapsulated file object into its JSON string representation
     * using the configured writer of the associated format.
     *
     * @return the JSON string representation of the file object
     * @throws JsonProcessingException if an error occurs during serialization
     */
    String asString() throws JsonProcessingException {
        return format.writer().writeValueAsString(file);
    }
}
