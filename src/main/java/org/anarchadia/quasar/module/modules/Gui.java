/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.module.modules;

import org.anarchadia.quasar.gui.QuasarGui;
import org.anarchadia.quasar.module.Module;
import org.lwjgl.glfw.GLFW;

public class Gui extends Module {

    public Gui() {
        super("Gui", "Quasar gui.", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.RENDER);
    }

    @Override
    public void toggle() {
        openGui();
    }

    public void openGui() {
        if (!QuasarGui.isOpen) {
            mc.setScreen(new QuasarGui());
            QuasarGui.isOpen = true;
        }
    }

}
