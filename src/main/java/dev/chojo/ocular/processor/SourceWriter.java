/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.ocular.processor;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple indentation-aware source code writer used by {@link OcularProcessor} to generate Java source files.
 * <p>
 * When the annotation processor needs to create a new {@code .java} file, it can't just dump raw text —
 * the generated code should be properly indented and readable (for debugging). This class wraps a
 * {@link Writer} and automatically manages indentation levels. Each call to {@link #beginBlock} increases
 * the indent (e.g. when opening a class or method body), and {@link #endBlock} decreases it.
 * <p>
 * The {@code {}} placeholder in {@link #println(String, Object...)} works like a simplified format string,
 * making it easy to insert variable names, types, and expressions into the generated code.
 */
public class SourceWriter implements AutoCloseable {

    private static final String INDENT_UNIT = "    ";

    private final Writer writer;
    private int indent;
    private String indentString;

    public SourceWriter(Writer writer) {
        this.writer = writer;
        this.indent = 0;
        this.indentString = "";
    }

    public void incrementIndent() {
        indent++;
        indentString = INDENT_UNIT.repeat(indent);
    }

    public void decrementIndent() {
        if (indent > 0) {
            indent--;
            indentString = INDENT_UNIT.repeat(indent);
        }
    }

    /**
     * Prints an indented line with optional format arguments, followed by a newline.
     * Use {@code {}} as placeholder (replaced by {@code %s} internally).
     */
    public void println(String format, Object... args) throws IOException {
        String line = format.replace("{}", "%s").formatted(args);
        if (!line.isBlank()) {
            writer.append(indentString);
            writer.append(line);
        }
        writer.append("\n");
    }

    /**
     * Prints an empty line.
     */
    public void println() throws IOException {
        writer.append("\n");
    }

    /**
     * Opens a block: prints the line and increments indent.
     * E.g. {@code beginBlock("public class Foo {")} prints the line then indents.
     */
    public void beginBlock(String format, Object... args) throws IOException {
        println(format, args);
        incrementIndent();
    }

    /**
     * Closes a block: decrements indent and prints the closing line.
     * E.g. {@code endBlock("}")} un-indents then prints {@code }}.
     */
    public void endBlock(String line) throws IOException {
        decrementIndent();
        println(line);
    }

    /**
     * Shorthand for {@code endBlock("}")}.
     */
    public void endBlock() throws IOException {
        endBlock("}");
    }

    public void close() throws IOException {
        writer.close();
    }
}
