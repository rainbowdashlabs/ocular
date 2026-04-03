/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.override;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a configuration field or method can be overridden by an environment variable.
 * <p>
 * Used inside {@link Overwrite @Overwrite} to declare an environment variable source, e.g.:
 * <pre>{@code
 * @Overwrite(env = @EnvVar("MY_APP_HOST"))
 * private String host;
 * }</pre>
 * <p>
 * If no explicit name is provided (i.e. {@code @EnvVar()}), the variable name is derived
 * automatically from the class name and field name in UPPER_CASE format:
 * {@code CLASSNAME_FIELDNAME}. For example, a field {@code myCoolVariable} in class
 * {@code AppConfig} would look for the environment variable {@code APPCONFIG_MYCOOLVARIABLE}.
 *
 * @see Overwrite
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EnvVar {
    /**
     * The environment variable name to read. Leave empty to use the auto-derived name.
     */
    String value() default "";
}
