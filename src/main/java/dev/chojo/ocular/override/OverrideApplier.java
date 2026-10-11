/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.override;

import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Applies override values from a {@link ValueSupplier} to a configuration object at runtime.
 * <p>
 * This is the runtime counterpart to the compile-time code generation done by
 * {@link dev.chojo.ocular.processor.OcularProcessor}. The overall flow is:
 * <ol>
 *   <li>At compile time, the annotation processor scans {@link Overwrite @Overwrite} annotations
 *       and generates a {@link ValueSupplier} class that reads env vars / system properties.</li>
 *   <li>At runtime, after a config file is deserialized, {@link dev.chojo.ocular.Configurations}
 *       loads the generated {@link ValueSupplier} via reflection.</li>
 *   <li>This class then iterates over every field and single-parameter method of the config object,
 *       asks the supplier if an override exists for that name, converts the string value to the
 *       correct type, and sets it on the object — effectively replacing the file-based value.</li>
 * </ol>
 */
public final class OverrideApplier {
    private static final Logger log = getLogger(OverrideApplier.class);

    private OverrideApplier() {
    }

    /**
     * Applies all available overrides from the given supplier to the configuration object.
     * <p>
     * For each declared field, it checks if the supplier has an override value for that field name.
     * If so, the value is converted from a string to the field's type and written directly into the field.
     * The same is done for single-parameter methods (e.g. setters), where the override value is
     * converted to the method's parameter type and the method is invoked.
     *
     * @param object   the configuration object whose fields/methods may be overridden
     * @param supplier the source of override values (typically a generated class)
     * @param <V>      the configuration type
     */
    public static <V> void applyOverrides(V object, ValueSupplier supplier) {
        if (object == null || supplier == null) return;

        // Get the class definition so we can inspect its fields and methods via reflection.
        // Reflection lets us read and write private fields at runtime, which is how we inject
        // override values without the config class needing any special code.
        Class<?> clazz = object.getClass();

        // Log available overrides with their descriptions
        logAvailableOverrides(clazz);

        // Walk through every field in the config class (e.g. "private String host")
        // and check if the generated supplier has an override value for that field name.
        for (Field field : clazz.getDeclaredFields()) {
            Optional<Object> override = supplier.getValue(field.getName());
            if (override.isPresent()) {
                try {
                    // By default, Java prevents access to private fields from outside the class.
                    // setAccessible(true) bypasses that restriction so we can write to it.
                    field.setAccessible(true);
                    // The override value comes as a String (from env / prop), but the field
                    // might be an int, boolean, etc. convertValue handles that conversion.
                    Object value = convertValue(field.getType(), field.getGenericType(), override.get());
                    if (value != null) {
                        // Actually write the override value into the field on the config object
                        field.set(object, value);
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Could not set override for field {}: {}", field.getName(), e.getMessage());
                } catch (NumberFormatException e) {
                    log.warn("Could not convert override value for field {}: {}", field.getName(), e.getMessage());
                }
            }
        }

        // Also check single-parameter methods (typically setters like "setHost(String host)").
        // We only consider methods with exactly one parameter, since those are the ones that
        // make sense as "set this value" operations.
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() != 1) continue;
            Optional<Object> override = supplier.getValue(method.getName());
            if (override.isPresent()) {
                try {
                    method.setAccessible(true);
                    // Convert the string value to match the method's parameter type
                    Object value = convertValue(method.getParameterTypes()[0], method.getGenericParameterTypes()[0], override.get());
                    if (value != null) {
                        // Call the method on the config object, passing the override value
                        method.invoke(object, value);
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Could not invoke override for method {}: {}", method.getName(), e.getMessage());
                } catch (NumberFormatException e) {
                    log.warn("Could not convert override value for method {}: {}", method.getName(), e.getMessage());
                } catch (java.lang.reflect.InvocationTargetException e) {
                    log.warn("Override method {} threw an exception: {}", method.getName(), e.getCause().getMessage());
                }
            }
        }

        // Handle @Overwrite annotations on zero-parameter methods (getters).
        // When a getter like "greetingValue()" is annotated, the override is keyed by the method
        // name. Since we can't "set" a value through a getter, we need to find the backing field.
        // Strategy: look for a field whose name the method name starts with (e.g. "greetingValue"
        // starts with "greeting"), or try stripping common getter prefixes like "get"/"is".
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() != 0) continue;
            Optional<Object> override = supplier.getValue(method.getName());
            if (override.isPresent()) {
                Field targetField = findBackingField(clazz, method.getName(), method.getReturnType());
                if (targetField != null) {
                    try {
                        targetField.setAccessible(true);
                        Object value = convertValue(targetField.getType(), targetField.getGenericType(), override.get());
                        if (value != null) {
                            targetField.set(object, value);
                        }
                    } catch (IllegalAccessException e) {
                        log.warn("Could not set override for getter {}: {}", method.getName(), e.getMessage());
                    } catch (NumberFormatException e) {
                        log.warn("Could not convert override value for getter {}: {}", method.getName(), e.getMessage());
                    }
                } else {
                    log.warn("Could not find backing field for getter method {}", method.getName());
                }
            }
        }
    }

    /**
     * Finds the backing field for a getter method by trying several strategies:
     * <ol>
     *   <li>Exact name match (method name equals field name)</li>
     *   <li>JavaBean getter convention: strip "get"/"is" prefix and lowercase first char</li>
     *   <li>Prefix match: find a field whose name the method name starts with,
     *       preferring the longest matching field name (e.g. "greetingValue" matches "greeting")</li>
     * </ol>
     * Only fields whose type is compatible with the method's return type are considered.
     */
    private static Field findBackingField(Class<?> clazz, String methodName, Class<?> returnType) {
        // Strategy 1: exact name match
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(methodName) && field.getType().equals(returnType)) {
                return field;
            }
        }

        // Strategy 2: JavaBean getter convention (getHost -> host, isDebug -> debug)
        String beanFieldName = null;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            beanFieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            beanFieldName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        if (beanFieldName != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(beanFieldName) && field.getType().equals(returnType)) {
                    return field;
                }
            }
        }

        // Strategy 3: find the field whose name is the longest prefix of the method name
        Field bestMatch = null;
        int bestLength = 0;
        for (Field field : clazz.getDeclaredFields()) {
            String fname = field.getName();
            if (methodName.startsWith(fname) && fname.length() > bestLength && field.getType().equals(returnType)) {
                bestMatch = field;
                bestLength = fname.length();
            }
        }
        return bestMatch;
    }

    /**
     * Logs all available overrides for the given configuration class, including their descriptions
     * if provided via the {@link Overwrite#description()} attribute.
     * Takes {@link OverwritePrefix} into account for deriving default keys and forced prefixes.
     */
    private static void logAvailableOverrides(Class<?> clazz) {
        String prefix = clazz.getSimpleName();
        boolean forcePrefix = false;
        OverwritePrefix overwritePrefix = clazz.getAnnotation(OverwritePrefix.class);
        if (overwritePrefix != null) {
            prefix = overwritePrefix.value();
            forcePrefix = overwritePrefix.force();
        }

        boolean headerLogged = false;
        for (Field field : clazz.getDeclaredFields()) {
            Overwrite overwrite = field.getAnnotation(Overwrite.class);
            if (overwrite != null) {
                if (!headerLogged) {
                    log.info("Available overrides for {}:", clazz.getSimpleName());
                    headerLogged = true;
                }
                logOverwriteSources(overwrite, prefix, forcePrefix, field.getName());
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            Overwrite overwrite = method.getAnnotation(Overwrite.class);
            if (overwrite != null) {
                if (!headerLogged) {
                    log.info("Available overrides for {}:", clazz.getSimpleName());
                    headerLogged = true;
                }
                logOverwriteSources(overwrite, prefix, forcePrefix, method.getName());
            }
        }
    }

    /**
     * Logs the property and environment variable sources for a single {@link Overwrite} annotation,
     * taking the prefix and force flag into account.
     */
    private static void logOverwriteSources(Overwrite overwrite, String prefix, boolean forcePrefix, String memberName) {
        String desc = overwrite.description().isEmpty() ? "" : " - " + overwrite.description();
        for (Prop prop : overwrite.prop()) {
            String propPrefix = prefix.replace("_", ".").toLowerCase();
            String key;
            if (prop.value().isEmpty()) {
                key = propPrefix + "." + memberName;
            } else if (forcePrefix) {
                key = propPrefix + "." + prop.value();
            } else {
                key = prop.value();
            }
            log.info("  Property: {}{}", key, desc);
        }
        for (Env env : overwrite.env()) {
            String envPrefix = prefix.replace(".", "_").toUpperCase();
            String key;
            if (env.value().isEmpty()) {
                key = envPrefix + "_" + memberName.toUpperCase();
            } else if (forcePrefix) {
                key = envPrefix + "_" + env.value();
            } else {
                key = env.value();
            }
            log.info("  Environment: {}{}", key, desc);
        }
    }

    /**
     * Converts a string override value to the target field/parameter type.
     * Supports all Java primitive types and their boxed equivalents, plus String.
     * Also supports arrays, {@link List} and {@link Set} of String, where the input
     * value is a comma-separated string.
     * Returns null for unsupported types (with a warning logged).
     */
    private static Object convertValue(Class<?> type, Type genericType, Object value) {
        if (value == null) return null;
        // Environment variables and system properties are always strings, so we need to parse
        // them into the correct Java type. For example, the string "8080" becomes the int 8080.
        String stringValue = value.toString();

        if (type == String.class) return stringValue;
        if (type == int.class || type == Integer.class) return Integer.parseInt(stringValue);
        if (type == long.class || type == Long.class) return Long.parseLong(stringValue);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(stringValue);
        if (type == double.class || type == Double.class) return Double.parseDouble(stringValue);
        if (type == float.class || type == Float.class) return Float.parseFloat(stringValue);
        if (type == short.class || type == Short.class) return Short.parseShort(stringValue);
        if (type == byte.class || type == Byte.class) return Byte.parseByte(stringValue);

        // Arrays, Lists and Sets are provided as comma-separated strings
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            String[] parts = stringValue.split(",", -1);
            Object array = Array.newInstance(componentType, parts.length);
            for (int i = 0; i < parts.length; i++) {
                Object element = convertValue(componentType, componentType, parts[i].trim());
                Array.set(array, i, element);
            }
            return array;
        }

        // Resolve the element type from the generic type parameter (e.g. List<Integer> -> Integer)
        Class<?> elementType = String.class;
        if (genericType instanceof ParameterizedType parameterized) {
            Type[] typeArgs = parameterized.getActualTypeArguments();
            if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> cls) {
                elementType = cls;
            }
        }

        if (type == List.class || type == ArrayList.class) {
            String[] parts = stringValue.split(",", -1);
            List<Object> list = new ArrayList<>();
            for (String part : parts) {
                list.add(convertValue(elementType, elementType, part.trim()));
            }
            return list;
        }
        if (type == Set.class || type == HashSet.class) {
            String[] parts = stringValue.split(",", -1);
            Set<Object> set = new HashSet<>();
            for (String part : parts) {
                set.add(convertValue(elementType, elementType, part.trim()));
            }
            return set;
        }

        log.warn("Unsupported override type: {}", type.getName());
        return null;
    }
}
