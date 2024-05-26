/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.modules;

import io.github.vialdevelopment.attendance.attender.Attender;
import org.anarchadia.quasar.api.event.events.TickEvent;
import org.anarchadia.quasar.api.module.Module;
import org.lwjgl.glfw.GLFW;

/* Example module */
public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Automatically sprints for you.", GLFW.GLFW_KEY_R, Category.MOVEMENT);
    }

    Attender<TickEvent> onTick = new Attender<>(TickEvent.class, event -> {
        if (mc.world == null || mc.player == null) return;

        if (mc.player.forwardSpeed > 0 && !mc.player.horizontalCollision && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    });
}
