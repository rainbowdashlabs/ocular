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
 * Specifies that a configuration field or method can be overridden by a JVM system property.
 * <p>
 * Used inside {@link Overwrite @Overwrite} to declare a system property source, e.g.:
 * <pre>{@code
 * @Overwrite(prop= @Prop("app.host"))
 * private String host;
 * }</pre>
 * <p>
 * If no explicit name is provided (i.e. {@code @Prop()}), the property name is derived
 * automatically from the class name (lowercased) and field name in dot notation:
 * {@code classname.fieldName}. For example, a field {@code myCoolVariable} in class
 * {@code AppConfig} would look for the system property {@code appconfig.myCoolVariable}
 * (set via {@code -Dappconfig.myCoolVariable=value} on the JVM command line).
 *
 * @see Overwrite
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Prop {
    /**
     * The system property name to read. Leave empty to use the auto-derived name.
     */
    String value() default "";
}
