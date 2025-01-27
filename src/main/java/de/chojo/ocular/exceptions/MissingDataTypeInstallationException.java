/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.chojo.ocular.exceptions;

public class MissingDataTypeInstallationException extends RuntimeException {
    public MissingDataTypeInstallationException(String dataType, String module) {
        super("Missing module for data type %s. Install %s to allow parsing of this format.".formatted(dataType, module));
    }
}
