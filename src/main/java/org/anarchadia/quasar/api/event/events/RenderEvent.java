/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.event.events;

import net.minecraft.client.util.math.MatrixStack;
import org.anarchadia.quasar.api.event.EventCancellable;

public class RenderEvent extends EventCancellable {
    protected float partialTicks;
    protected MatrixStack matrixStack;

    public RenderEvent(EventStage stage, float partialTicks, MatrixStack matrixStack) {
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
    }

    /**
     * Gets the partial ticks.
     *
     * @return partial ticks
     */
    public float getPartialTicks() {
        return partialTicks;
    }

    /**
     * Gets the matrix stack.
     *
     * @return matrix stack
     */
    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
}
