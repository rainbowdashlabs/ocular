/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.ocular.override.OverrideApplier;
import dev.chojo.ocular.override.ValueSupplier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverrideApplierTest {

    static class FieldTarget {
        String name = "original";
        int count = 0;
        boolean active = false;
        double rate = 1.0;
    }

    static class MethodTarget {
        private String name = "original";
        private int count = 0;

        public String getName() { return name; }
        public int getCount() { return count; }

        public void name(String name) { this.name = name; }
        public void count(int count) { this.count = count; }
    }

    static class MapSupplier implements ValueSupplier {
        private final Map<String, String> values;

        MapSupplier(Map<String, String> values) {
            this.values = values;
        }

        @Override
        public Optional<Object> getValue(String fieldOrMethodName) {
            return Optional.ofNullable(values.get(fieldOrMethodName));
        }
    }

    @Test
    void fieldOverride() {
        FieldTarget target = new FieldTarget();
        MapSupplier supplier = new MapSupplier(Map.of(
                "name", "overridden",
                "count", "42",
                "active", "true",
                "rate", "3.14"
        ));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals("overridden", target.name);
        assertEquals(42, target.count);
        assertEquals(true, target.active);
        assertEquals(3.14, target.rate, 0.001);
    }

    @Test
    void methodOverride() {
        MethodTarget target = new MethodTarget();
        MapSupplier supplier = new MapSupplier(Map.of(
                "name", "via-method",
                "count", "99"
        ));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals("via-method", target.getName());
        assertEquals(99, target.getCount());
    }

    @Test
    void fieldAndMethodBothApplied() {
        // When both field and method match, both are applied.
        // Method runs after field, so method's effect is the final state.
        MethodTarget target = new MethodTarget();
        MapSupplier supplier = new MapSupplier(Map.of("name", "both"));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals("both", target.getName());
    }

    @Test
    void noOverrideWhenKeyMissing() {
        FieldTarget target = new FieldTarget();
        MapSupplier supplier = new MapSupplier(Map.of());

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals("original", target.name);
        assertEquals(0, target.count);
    }

    @Test
    void nullObjectDoesNotThrow() {
        MapSupplier supplier = new MapSupplier(Map.of("name", "value"));
        OverrideApplier.applyOverrides(null, supplier);
    }

    @Test
    void nullSupplierDoesNotThrow() {
        FieldTarget target = new FieldTarget();
        OverrideApplier.applyOverrides(target, null);
        assertEquals("original", target.name);
    }

    @Test
    void invalidNumberDoesNotThrow() {
        FieldTarget target = new FieldTarget();
        MapSupplier supplier = new MapSupplier(Map.of("count", "not-a-number"));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals(0, target.count);
    }

    @Test
    void precedenceLastWriteWins() {
        // Simulates what the generated code does: later puts override earlier ones in the map.
        // If both sys and env set the same key, the last one in the map wins.
        Map<String, String> values = new HashMap<>();
        values.put("name", "first");
        values.put("name", "second"); // overwrites
        MapSupplier supplier = new MapSupplier(values);

        FieldTarget target = new FieldTarget();
        OverrideApplier.applyOverrides(target, supplier);

        assertEquals("second", target.name);
    }
}
