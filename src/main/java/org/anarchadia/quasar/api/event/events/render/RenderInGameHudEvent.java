/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.event.events.render;

import net.minecraft.client.gui.DrawContext;
import org.anarchadia.quasar.api.event.EventCancellable;

public class RenderInGameHudEvent extends EventCancellable {
    private final DrawContext context;

    public RenderInGameHudEvent(DrawContext context) {
        this.context = context;
    }

    /**
     * Gets the draw context.
     *
     * @return context
     */
    public DrawContext getContext() {
        return context;
    }
}
