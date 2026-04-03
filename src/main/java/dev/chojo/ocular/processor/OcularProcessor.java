/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.processor;

import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.OverwritePrefix;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compile-time annotation processor that generates override helper classes for configuration objects.
 * <p>
 * <b>What this does in plain terms:</b> When you annotate fields in your config class with
 * {@link Overwrite @Overwrite}, this processor runs automatically during compilation (not at runtime).
 * It scans your source code for those annotations and writes a brand-new Java source file for each
 * config class that has them. That generated file is compiled alongside your code and becomes part
 * of your application.
 * <p>
 * <b>What the generated class looks like:</b> For a config class {@code com.example.MyConfig},
 * the processor creates {@code com.example.MyConfig_OcularOverride}. This generated class:
 * <ol>
 *   <li>Implements {@link dev.chojo.ocular.override.ValueSupplier ValueSupplier}.</li>
 *   <li>In its constructor, reads the relevant environment variables and system properties
 *       (as declared in the {@code @Overwrite} annotations) and stores any found values in a map.</li>
 *   <li>Provides a {@code getValue(fieldName)} method that returns the override value for a given field,
 *       or empty if no override was set.</li>
 * </ol>
 * <p>
 * <b>How it connects to the rest of the system:</b>
 * <ol>
 *   <li>This processor generates the code at compile time.</li>
 *   <li>At runtime, {@link dev.chojo.ocular.Configurations Configurations} loads the generated class via reflection
 *       (by looking for {@code ClassName_OcularOverride}).</li>
 *   <li>{@link dev.chojo.ocular.override.OverrideApplier OverrideApplier} then uses the generated supplier to
 *       overwrite fields in the deserialized config object.</li>
 * </ol>
 * <p>
 * <b>Registration:</b> This processor is registered via the standard Java service loader mechanism
 * in {@code META-INF/services/javax.annotation.processing.Processor}, so the compiler discovers
 * it automatically.
 */
// These two annotations tell the Java compiler which annotations this processor cares about
// and which Java version it targets. Without them, the compiler wouldn't know to invoke this class.
@SupportedAnnotationTypes("dev.chojo.ocular.override.Overwrite")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class OcularProcessor extends AbstractProcessor {

    // The Filer is the compiler's API for creating new source files on disk during compilation.
    private Filer filer;
    // The Messager lets us print errors/warnings that show up in the compiler output (like javac warnings).
    private Messager messager;

    /**
     * Writes a single lookup block into the generated constructor.
     * The generated code reads a value (from env or prop) and, if non-null, stores it in the map.
     */
    private static void emitLookup(SourceWriter out, String fieldName, String lookupExpression) throws IOException {
        // This writes a small block of Java code into the generated constructor. For example,
        // for a field "host" with System.getenv("MY_HOST"), the generated code would be:
        //
        //   {
        //       String value = System.getenv("MY_HOST");
        //       if (value != null && !overrides.containsKey("host")) overrides.put("host", value);
        //   }
        //
        // The braces create a local scope so the "value" variable doesn't conflict between lookups.
        // If the env / prop is set, it gets stored in the map keyed by the field name.
        // If multiple lookups target the same field, the first non-null one wins (later lookups are skipped).
        out.beginBlock("{");
        out.println("String value = {};", lookupExpression);
        out.println("if (value != null && !overrides.containsKey(\"{}\")) overrides.put(\"{}\", value);", fieldName, fieldName);
        out.endBlock();
    }

    /**
     * Called once by the compiler before processing begins.
     * We grab references to the {@link Filer} (used to create new source files)
     * and the {@link Messager} (used to report errors/warnings back to the compiler).
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    /**
     * Main entry point called by the compiler for each processing round.
     * <p>
     * Step 1: Find all fields/methods annotated with {@code @Overwrite} and group them by
     * their enclosing class (the config class they belong to).
     * Step 2: For each config class, generate a new {@code _OcularOverride} source file.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Group all @Overwrite-annotated elements by the class they belong to
        Map<TypeElement, List<Element>> classesToProcess = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(Overwrite.class)) {
            TypeElement enclosingClass = (TypeElement) element.getEnclosingElement();
            classesToProcess.computeIfAbsent(enclosingClass, k -> new ArrayList<>()).add(element);
        }

        // Collect override info for the reference file
        Map<String, List<OverrideInfo>> allOverrides = new LinkedHashMap<>();

        // Generate one override provider class per config class
        for (Map.Entry<TypeElement, List<Element>> entry : classesToProcess.entrySet()) {
            try {
                List<OverrideInfo> infos = generateOverrideProvider(entry.getKey(), entry.getValue());
                allOverrides.put(entry.getKey().getQualifiedName().toString(), infos);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Could not generate override provider for " + entry.getKey().getQualifiedName() + ": " + e.getMessage());
            }
        }

        // Generate reference documentation file
        if (!allOverrides.isEmpty()) {
            try {
                generateOverrideDocumentation(allOverrides);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Could not generate override documentation: " + e.getMessage());
            }
        }

        return true;
    }

    /**
     * Generates the {@code _OcularOverride} Java source file for a single config class.
     * <p>
     * The generated class implements {@link dev.chojo.ocular.override.ValueSupplier} and contains:
     * <ul>
     *   <li>A {@code Map<String, String>} that maps field names to their override values.</li>
     *   <li>A constructor that reads env vars / system properties and populates the map.</li>
     *   <li>A {@code getValue()} method that looks up a field name in the map.</li>
     * </ul>
     */
    /**
     * Holds information about a single override for documentation and logging purposes.
     */
    private record OverrideInfo(String fieldName, String description, List<String> sources) {
    }

    /**
     * Generates a Markdown reference file at {@code META-INF/ocular/overrides.md} listing all
     * overridable properties and environment variables with their descriptions.
     */
    private void generateOverrideDocumentation(Map<String, List<OverrideInfo>> allOverrides) throws IOException {
        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/ocular/overrides.md");
        try (Writer writer = resource.openWriter()) {
            writer.append("# Ocular Override Reference\n\n");
            writer.append("This file is auto-generated by the Ocular annotation processor.\n");
            writer.append("It lists all available configuration overrides.\n\n");

            for (Map.Entry<String, List<OverrideInfo>> entry : allOverrides.entrySet()) {
                writer.append("## ").append(entry.getKey()).append("\n\n");
                for (OverrideInfo info : entry.getValue()) {
                    writer.append("### ").append(info.fieldName()).append("\n\n");
                    if (!info.description().isEmpty()) {
                        writer.append(info.description()).append("\n\n");
                    }
                    writer.append("Sources (in priority order):\n");
                    for (String source : info.sources()) {
                        writer.append("- ").append(source).append("\n");
                    }
                    writer.append("\n");
                }
            }
        }
    }

    private List<OverrideInfo> generateOverrideProvider(TypeElement typeElement, List<Element> elements) throws IOException {
        // Extract the package (e.g. "com.example") so we can put the generated file in the same package
        String packageName = ((PackageElement) typeElement.getEnclosingElement()).getQualifiedName().toString();
        // Full name like "com.example.MyConfig"
        String fullClassName = typeElement.getQualifiedName().toString();

        // Simple name like "MyConfig" — used for deriving default env / prop names
        // Check if the class has @OverwritePrefix to override the class name prefix
        // and optionally force it onto explicitly provided names too.
        String prefix = typeElement.getSimpleName().toString();
        boolean forcePrefix = false;
        OverwritePrefix overwritePrefix = typeElement.getAnnotation(OverwritePrefix.class);
        if (overwritePrefix != null) {
            prefix = overwritePrefix.value();
            forcePrefix = overwritePrefix.force();
        }
        // The name of the class we're about to generate, e.g. "MyConfig_OcularOverride"
        // For inner classes like "Outer.Inner", dots become underscores: "Outer_Inner_OcularOverride"
        String generatedClassName = fullClassName.substring(packageName.length() + 1).replace('.', '_') + "_OcularOverride";

        // Ask the compiler to create a new .java source file. The compiler will automatically
        // compile this generated file in the same compilation round — we just need to write valid Java into it.
        // Collect override info for documentation
        List<OverrideInfo> overrideInfos = new ArrayList<>();

        JavaFileObject builderFile = filer.createSourceFile(packageName + "." + generatedClassName);
        // SourceWriter is a helper that handles indentation so the generated code is readable
        try (SourceWriter out = new SourceWriter(builderFile.openWriter())) {
            out.println("package {};", packageName);
            out.println();
            out.println("import dev.chojo.ocular.override.ValueSupplier;");
            out.println("import java.util.Optional;");
            out.println();

            out.beginBlock("public class {} implements ValueSupplier {", generatedClassName);
            out.println();
            out.println("private final java.util.Map<String, String> overrides = new java.util.HashMap<>();");
            out.println();

            // The constructor is where all the work happens in the generated class.
            // For each field that had @Overwrite, we write Java code that reads the env or prop
            // and stores the result in the "overrides" map. This code runs once when the class is instantiated.
            out.beginBlock("public {}() {", generatedClassName);
            for (Element element : elements) {
                String fieldName = element.getSimpleName().toString();
                // Write the lookup code for this field's env/prop sources into the constructor
                OverrideInfo info = emitLookupsInOrder(out, element, prefix, forcePrefix, fieldName);
                if (info != null) {
                    overrideInfos.add(info);
                }
            }
            out.endBlock();

            out.println();
            out.println("@Override");
            out.beginBlock("public Optional<Object> getValue(String fieldOrMethodName) {");
            out.println("return Optional.ofNullable(overrides.get(fieldOrMethodName));");
            out.endBlock();

            out.endBlock();
        }
        return overrideInfos;
    }

    /**
     * Writes Java code into the generated constructor that looks up override values for one field.
     * <p>
     * The lookups are emitted in the exact order the user declared {@code sys} and {@code env}
     * in their {@code @Overwrite} annotation. This matters because the first non-null lookup
     * wins — once a value is set for a field, subsequent lookups do not overwrite it.
     * <p>
     * We use "annotation mirrors" here instead of calling annotation methods directly. This is
     * necessary because annotation processors run at compile time and can only inspect the source
     * code structure (mirrors), not the actual runtime annotation objects. Mirrors preserve the
     * declaration order of attributes, which is essential for correct precedence.
     */
    private OverrideInfo emitLookupsInOrder(SourceWriter out, Element element, String prefix, boolean forcePrefix, String fieldName) throws IOException {
        // Get the compile-time representation ("mirror") of the @Overwrite annotation on this field.
        // We need the mirror (not the annotation object) because at compile time the annotation class
        // isn't loaded as a real object — it only exists as metadata in the source code.
        AnnotationMirror overwriteMirror = findOverwriteMirror(element);
        if (overwriteMirror == null) return null;

        // Extract the description from the @Overwrite annotation
        String description = extractStringValue(overwriteMirror, "description");
        if (description == null) description = "";
        List<String> sources = new ArrayList<>();

        // Walk through each attribute of @Overwrite in the order the user wrote them.
        // For example, in @Overwrite(prop = @Prop(), env = @Env()), we'd iterate:
        //   1. "prop" -> [@Prop()]
        //   2. "env" -> [@Env()]
        // The iteration order matches the source code order, which determines override precedence.
        // The first source that provides a non-null value wins.
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                overwriteMirror.getElementValues().entrySet()) {
            // Skip the description value
            if (entry.getValue().getValue().getClass() == String.class) continue;
            // attrName is either "prop" or "env" — the attribute name from @Overwrite
            String attrName = entry.getKey().getSimpleName().toString();
            // The value is an array of nested annotations (e.g. the @Prop[] or @Env[] array)
            @SuppressWarnings("unchecked")
            List<? extends AnnotationValue> values = (List<? extends AnnotationValue>) entry.getValue().getValue();
            if ("prop".equals(attrName)) {
                // make sure that the prefix is adjusted for whatever input format is used.
                prefix = prefix.replace("_", ".").toLowerCase();
                // For each @Prop in the array, generate code that reads a JVM system property
                for (AnnotationValue av : values) {
                    // Each element in the array is itself an annotation mirror (the @Prop annotation)
                    AnnotationMirror sysMirror = (AnnotationMirror) av.getValue();
                    // Read the "value" attribute of @Prop (the custom property name, if provided)
                    String key = extractStringValue(sysMirror, "value");
                    if (key == null || key.isEmpty()) {
                        // No custom name provided — derive default: "prefix.fieldName"
                        key = prefix + "." + fieldName;
                    } else if (forcePrefix) {
                        // Force mode: always prepend the prefix, even for explicit names
                        key = prefix + "." + key;
                    }
                    // Write Java code like: String value = System.getProperty("myclass.host");
                    emitLookup(out, fieldName, "System.getProperty(\"" + key + "\")");
                    sources.add("Property: `" + key + "`");
                }
            } else if ("env".equals(attrName)) {
                prefix = prefix.replace(".", "_").toUpperCase();
                // For each @Env in the array, generate code that reads an environment variable
                for (AnnotationValue av : values) {
                    AnnotationMirror envMirror = (AnnotationMirror) av.getValue();
                    String key = extractStringValue(envMirror, "value");
                    if (key == null || key.isEmpty()) {
                        // No custom name provided — derive default: "PREFIX_FIELDNAME"
                        key = prefix + "_" + fieldName.toUpperCase();
                    } else if (forcePrefix) {
                        // Force mode: always prepend the prefix, even for explicit names
                        key = prefix + "_" + key;
                    }
                    // Write Java code like: String value = System.getenv("MYCLASS_HOST");
                    emitLookup(out, fieldName, "System.getenv(\"" + key + "\")");
                    sources.add("Environment: `" + key + "`");
                }
            }
        }
        return new OverrideInfo(fieldName, description, sources);
    }

    /**
     * Finds the {@code @Overwrite} annotation mirror on the given element.
     * A "mirror" is the compile-time representation of an annotation — it lets us
     * inspect annotation attributes in their declared order.
     */
    private AnnotationMirror findOverwriteMirror(Element element) {
        // An element (field/method) can have multiple annotations. We loop through all of them
        // to find the one that is @Overwrite. We compare by fully qualified name because at
        // compile time we're working with type metadata, not actual annotation instances.
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(Overwrite.class.getCanonicalName())) {
                return mirror;
            }
        }
        return null;
    }

    /**
     * Reads a string attribute value from an annotation mirror (e.g. the {@code value} of {@code @Env("MY_VAR")}).
     */
    private String extractStringValue(AnnotationMirror mirror, String attributeName) {
        // Annotation attributes are stored as key-value pairs in the mirror.
        // The key is an ExecutableElement (representing the annotation method like "value()"),
        // and the value is the actual value the user provided (e.g. "MY_VAR" in @Env("MY_VAR")).
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                return entry.getValue().getValue().toString();
            }
        }
        return null;
    }
}
