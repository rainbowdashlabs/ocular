/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular;

import dev.chojo.ocular.override.OverrideApplier;
import dev.chojo.ocular.override.ValueSupplier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideApplierTest {

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
        assertTrue(target.active);
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
    void applierUsesSupplierValue() {
        // OverrideApplier simply applies whatever the ValueSupplier provides.
        // Precedence (first-wins) is enforced in the generated code, not in the applier.
        MapSupplier supplier = new MapSupplier(Map.of("name", "supplied"));

        FieldTarget target = new FieldTarget();
        OverrideApplier.applyOverrides(target, supplier);

        assertEquals("supplied", target.name);
    }

    @Test
    void stringArrayOverride() {
        CollectionTarget target = new CollectionTarget();
        MapSupplier supplier = new MapSupplier(Map.of("tags", "a,b,c"));

        OverrideApplier.applyOverrides(target, supplier);

        assertArrayEquals(new String[]{"a", "b", "c"}, target.tags);
    }

    @Test
    void intArrayOverride() {
        CollectionTarget target = new CollectionTarget();
        MapSupplier supplier = new MapSupplier(Map.of("numbers", "1,2,3"));

        OverrideApplier.applyOverrides(target, supplier);

        assertArrayEquals(new int[]{1, 2, 3}, target.numbers);
    }

    @Test
    void listOverride() {
        CollectionTarget target = new CollectionTarget();
        MapSupplier supplier = new MapSupplier(Map.of("names", "alice,bob,charlie"));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals(List.of("alice", "bob", "charlie"), target.names);
    }

    @Test
    void listOfIntegersOverride() {
        CollectionTarget target = new CollectionTarget();
        MapSupplier supplier = new MapSupplier(Map.of("counts", "10,20,30"));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals(List.of(10, 20, 30), target.counts);
    }

    @Test
    void setOverride() {
        CollectionTarget target = new CollectionTarget();
        MapSupplier supplier = new MapSupplier(Map.of("roles", "admin,user,guest"));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals(Set.of("admin", "user", "guest"), target.roles);
    }

    @Test
    void setOfIntegersOverride() {
        CollectionTarget target = new CollectionTarget();
        MapSupplier supplier = new MapSupplier(Map.of("ids", "1,2,3"));

        OverrideApplier.applyOverrides(target, supplier);

        assertEquals(Set.of(1, 2, 3), target.ids);
    }

    static class CollectionTarget {
        String[] tags = {};
        int[] numbers = {};
        List<String> names = List.of();
        List<Integer> counts = List.of();
        Set<String> roles = Set.of();
        Set<Integer> ids = Set.of();
    }

    static class FieldTarget {
        String name = "original";
        int count = 0;
        boolean active = false;
        double rate = 1.0;
    }

    static class MethodTarget {
        private String name = "original";
        private int count = 0;

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public void name(String name) {
            this.name = name;
        }

        public void count(int count) {
            this.count = count;
        }
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
}
