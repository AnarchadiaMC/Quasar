/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.module;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.client.KeyEvent;
import net.minecraft.client.util.InputUtil;
import org.anarchadia.quasar.impl.modules.Movement.Fly;
import org.anarchadia.quasar.impl.modules.Movement.Sprint;
import org.anarchadia.quasar.impl.modules.Render.GUI;
import org.anarchadia.quasar.impl.modules.Render.Hud;
import org.anarchadia.quasar.impl.modules.Render.RenderTest;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final ArrayList<Module> modules;
    private Module currentlyBindingModule = null;

    public ModuleManager() {
        modules = new ArrayList<>();

        /* Add modules here */
        modules.add(new Fly());
        modules.add(new GUI());
        modules.add(new Hud());
        modules.add(new RenderTest());
        modules.add(new Sprint());
    }

    /**
     * Gets the modules.
     */
    public ArrayList<Module> getModules() {
        return modules;
    }

    /**
     * Gets enabled modules.
     */
    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled())
                enabledModules.add(module);
        }
        return enabledModules;
    }

    /**
     * Gets the module by name.
     *
     * @param name name of the module
     */
    public Module getModule(String name) {
        return modules.stream().filter(mm -> mm.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Gets the modules state
     *
     * @param name name of the module
     */
    public boolean isModuleEnabled(String name) {
        Module mod = modules.stream().filter(mm -> mm.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        return mod.isEnabled();
    }

    /**
     * Gets the modules by category.
     *
     * @param category category of the module
     */
    public List<Module> getModulesByCategory(Module.Category category) {
        List<Module> cats = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) cats.add(m);
        }
        return cats;
    }

    public void onKeyPress(KeyEvent event) {
        if (InputUtil.isKeyPressed(Quasar.mc.getWindow().getHandle(), GLFW.GLFW_KEY_F3)) return;

        if (currentlyBindingModule != null) {
            if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
                // Cancel binding
                currentlyBindingModule = null;
            } else {
                currentlyBindingModule.setKey(event.getKey());
                currentlyBindingModule = null;
            }
            return;
        }

        modules.stream().filter(m -> m.getKey() == event.getKey()).forEach(Module::toggle);
    }

    public Module getCurrentlyBindingModule() {
        return currentlyBindingModule;
    }

    public void setCurrentlyBindingModule(Module module) {
        this.currentlyBindingModule = module;
    }
}
