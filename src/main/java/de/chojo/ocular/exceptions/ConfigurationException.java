/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.exceptions;

/**
 * Represents an exception that occurs due to configuration-related issues.
 * <p>
 * This exception is a subclass of {@code RuntimeException}, making it an unchecked exception.
 * It is typically thrown when a configuration operation fails or encounters an error,
 * providing an explanatory message and the underlying cause for the failure.
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
