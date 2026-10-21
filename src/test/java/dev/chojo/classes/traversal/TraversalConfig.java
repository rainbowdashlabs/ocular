/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes.traversal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.chojo.classes.NestedDatabase;

import java.util.logging.Level;

public class TraversalConfig {
    public NestedDatabase database = new NestedDatabase();

    @JsonIgnore
    public Level level = Level.WARNING;
}
