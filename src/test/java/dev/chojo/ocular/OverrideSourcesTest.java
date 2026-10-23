/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.ocular.override.ValueSupplier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverrideSourcesTest {
    @Test
    void sourcesFollowTheDeclaredOrder() throws ReflectiveOperationException {
        ValueSupplier supplier = (ValueSupplier) Class.forName("dev.chojo.classes.AnnotationConfig_OcularOverride")
                .getDeclaredConstructor()
                .newInstance();

        assertEquals(
                List.of("Property: annotationconfig.test", "Environment: ANNOTATIONCONFIG_TEST"),
                supplier.sources().get("test"));
        assertEquals(
                List.of("Environment: MY_ENV", "Property: my.sys"),
                supplier.sources().get("envFirst"));
    }
}
