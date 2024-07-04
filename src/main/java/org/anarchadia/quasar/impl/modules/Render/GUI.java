/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.modules.Render;

import org.anarchadia.quasar.impl.gui.QuasarGUI;
import org.anarchadia.quasar.api.module.Module;
import org.lwjgl.glfw.GLFW;

public class GUI extends Module {

    public GUI() {
        super("GUI", "Quasar GUI.", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.RENDER);
    }

    @Override
    public void toggle() {
        if (!QuasarGUI.isOpen) {
            QuasarGUI.isOpen = true;
            mc.setScreen(new QuasarGUI());
        }
    }
}
