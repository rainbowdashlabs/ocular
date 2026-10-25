/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.exceptions;

/**
 * Thrown when a data format is used whose Jackson module is not on the class path.
 */
public class MissingDataTypeInstallationException extends RuntimeException {
    /**
     * Creates the exception.
     *
     * @param dataType the data format that was requested
     * @param module   the module that has to be installed for it
     */
    public MissingDataTypeInstallationException(String dataType, String module) {
        super("Missing module for data type %s. Install %s to allow parsing of this format."
                .formatted(dataType, module));
    }
}
