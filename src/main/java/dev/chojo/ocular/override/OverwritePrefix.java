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
 * Overrides the class name used as the prefix when deriving default environment variable
 * and system property names for {@link Overwrite @Overwrite}-annotated fields.
 * <p>
 * By default, the simple class name is used as the prefix (e.g. {@code APPCONFIG_FIELDNAME}
 * for env vars, {@code appconfig.fieldName} for props). This annotation lets you
 * replace that prefix with a custom value.
 * <p>
 * Example — custom prefix:
 * <pre>{@code
 * @OverwritePrefix("myapp")
 * public class AppConfig {
 *     @Overwrite(env = @Env())
 *     public String host;  // looks for MYAPP_HOST instead of APPCONFIG_HOST
 * }
 * }</pre>
 * <p>
 * Example — forced prefix (always prepended, even when an explicit name is given):
 * <pre>{@code
 * @OverwritePrefix(value = "myapp", force = true)
 * public class AppConfig {
 *     @Overwrite(env = @Env("HOST"))
 *     public String host;  // looks for MYAPP_HOST instead of just HOST
 *
 *     @Overwrite(prop= @Prop("port"))
 *     public int port;     // looks for myapp.port instead of just port
 * }
 * }</pre>
 * When {@code force} is {@code false} (the default), explicitly provided names in
 * {@link Env @Env} and {@link Prop @Prop} are used as-is without any prefix.
 *
 * @see Overwrite
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OverwritePrefix {
    /**
     * The custom prefix to use instead of the class name.
     */
    String value();

    /**
     * When {@code true}, the prefix is always prepended to the key — even when the user
     * provides an explicit name in {@link Env @Env} or {@link Prop @Prop}.
     * <p>
     * For env vars, the result is {@code PREFIX_NAME}. For props, it is {@code prefix.name}.
     * <p>
     * Defaults to {@code false}.
     */
    boolean force() default false;
}
