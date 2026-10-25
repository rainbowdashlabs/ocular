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
 * Marks a field or method in a configuration class as overridable at runtime.
 * <p>
 * When a configuration class contains fields or methods annotated with {@code @Overwrite},
 * the annotation processor ({@link dev.chojo.ocular.processor.OcularProcessor OcularProcessor}) will generate
 * a helper class at compile time that knows how to look up override values from environment
 * variables and/or system properties.
 * <p>
 * You can specify one or more {@link Env} and/or {@link Prop} sources. The order you
 * declare them matters: the first source that provides a value wins. For example:
 * <pre>{@code
 * @Overwrite(prop = @Prop(), env = @Env())
 * private String host;
 * }</pre>
 * This will first check the system property, then the environment variable. If both are set,
 * the system property wins because it is declared first.
 * <p>
 * By default, an applied override becomes part of the configuration and is written to the file when it
 * is saved. Set {@link #persist()} to {@code false} to keep the value from the file instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Overwrite {
    /**
     * Environment variable sources to check for an override value.
     *
     * @return the environment variable sources
     */
    Env[] env() default {};

    /**
     * System property sources to check for an override value.
     *
     * @return the system property sources
     */
    Prop[] prop() default {};

    /**
     * Description of the override.
     *
     * @return the description, shown in the log and in the generated reference
     */
    String description() default "";

    /**
     * Whether an applied override is written to the file when the configuration is saved.
     * <p>
     * When {@code false}, saving writes the value that was read from the file, as long as the member still holds
     * the override value. A value assigned by code after loading is written as usual.
     *
     * @return {@code false} to write the file value instead of the override
     */
    boolean persist() default true;
}
