/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.classes;

import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

public class HeldConfig {
    @Overwrite(prop = @Prop("held.token"), persist = false)
    public String token = "file-token";

    @Overwrite(prop = @Prop("held.name"))
    public String name = "file-name";

    public HeldSection section = new HeldSection();
}
