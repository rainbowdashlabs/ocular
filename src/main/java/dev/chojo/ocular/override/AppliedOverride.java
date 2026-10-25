/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.override;

import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * An override that was applied to a field of a loaded configuration.
 * <p>
 * It remembers the value read from the file and the value the override put in its place. That allows writing the
 * file value instead of the override when the override is declared with {@link Overwrite#persist() persist = false},
 * and warning when code changes the field, since the override replaces that change again on the next load.
 */
public final class AppliedOverride {
    private static final Logger log = getLogger(AppliedOverride.class);
    private final Object target;
    private final Field field;
    private final Object fileValue;
    private final Object overrideValue;
    private final boolean persist;
    private boolean changeReported;

    AppliedOverride(Object target, Field field, Object fileValue, Object overrideValue, boolean persist) {
        this.target = target;
        this.field = field;
        this.fileValue = fileValue;
        this.overrideValue = overrideValue;
        this.persist = persist;
    }

    /**
     * Logs a warning once if code assigned a value other than the override value to the field. The change has no
     * lasting effect, because the override is applied again whenever the configuration is loaded.
     */
    public void warnIfChanged() {
        if (changeReported || !isChanged()) return;
        changeReported = true;
        log.warn(
                "{}.{} was changed, but it is set by an environment variable or system property. The new value is "
                        + "saved to the file, but the override takes precedence whenever the configuration is loaded.",
                target.getClass().getSimpleName(),
                field.getName());
    }

    /**
     * Puts the file value back into the field, if the override is not persisted and the field still holds the
     * override value.
     *
     * @return {@code true} if the file value was put back and {@link #reapplyOverride()} has to follow the write
     */
    public boolean restoreFileValue() {
        if (persist || isChanged()) return false;
        return set(fileValue);
    }

    /**
     * Puts the override value back into the field after a write.
     */
    public void reapplyOverride() {
        set(overrideValue);
    }

    private boolean isChanged() {
        try {
            return !Objects.deepEquals(field.get(target), overrideValue);
        } catch (IllegalAccessException e) {
            log.warn("Could not read override field {}: {}", field.getName(), e.getMessage());
            return false;
        }
    }

    private boolean set(Object value) {
        try {
            field.set(target, value);
            return true;
        } catch (IllegalAccessException e) {
            log.warn("Could not set override field {}: {}", field.getName(), e.getMessage());
            return false;
        }
    }
}
