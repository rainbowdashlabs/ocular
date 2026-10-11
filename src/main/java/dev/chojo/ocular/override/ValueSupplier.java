/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.override;

import java.util.Optional;

/**
 * Provides override values for configuration fields and methods.
 * <p>
 * Implementations of this interface are generated automatically at compile time by
 * {@link dev.chojo.ocular.processor.OcularProcessor} for each configuration class that uses
 * {@link Overwrite @Overwrite} annotations. The generated class reads environment variables
 * and system properties during construction and stores them in an internal map.
 * <p>
 * At runtime, when a configuration is loaded, {@link OverrideApplier} calls
 * {@link #getValue(String)} for each field/method name to check if an override exists.
 *
 * @see OverrideApplier
 * @see dev.chojo.ocular.processor.OcularProcessor
 */
public interface ValueSupplier {
    /**
     * Returns the override value for the given field or method name, if one was found
     * in the environment variables or system properties at construction time.
     *
     * @param fieldOrMethodName the name of the field or setter method to look up
     * @return an {@link Optional} containing the override value as a string, or empty if no override exists
     */
    Optional<Object> getValue(String fieldOrMethodName);
}
