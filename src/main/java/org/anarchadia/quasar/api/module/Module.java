/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.module;

import org.anarchadia.quasar.Quasar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.setting.settings.KeybindSetting;
import org.anarchadia.quasar.api.util.QuasarLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class Module {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public String name, description;
    public KeybindSetting keyCode = new KeybindSetting(0);
    public Category category;
    public boolean enabled;
    public List<Setting> settings = new ArrayList<>();

    /**
     * Module constructor.
     *
     * @param name name of the module.
     * @param description description of the module.
     * @param key GLFW keybinding.
     * @param category module category.
     *
     * @implNote addSettings() must be called in the constructor, otherwise the module will not have its settings.
     */
    public Module(String name, String description, int key, Category category) {
        super();
        this.name = name;
        this.description = description;
        keyCode.code = key;
        this.category = category;

        /* Add default settings */
        addSettings(keyCode);
    }


    /**
     * Adds settings to the module.
     * Must be called in the constructor.
     *
     * @param settings settings to add
     */
    public void addSettings(Setting... settings) {
        this.settings.addAll(Arrays.asList(settings));
        this.settings.sort(Comparator.comparingInt(s -> s == keyCode ? 1 : 0));
    }

    /**
     * Called when the module is enabled.
     */
    public void onEnable() {
        Quasar.getInstance().getEventManager().addEventListener(this);
        Quasar.getInstance().getConfigManager().save();

        QuasarLogger.info(Formatting.GREEN + "Enabled " + this.getName() + "!");
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable() {
        Quasar.getInstance().getEventManager().removeEventListener(this);
        Quasar.getInstance().getConfigManager().save();

        QuasarLogger.info(Formatting.RED + "Disabled " + this.getName() + "!");
    }

    /**
     * Toggles a module.
     */
    public void toggle() {
        this.enabled = !this.enabled;

        if (this.enabled) onEnable();
        else onDisable();
    }

    /**
     * Gets the enabled state of the module.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the enabled state of the module.
     *
     * @param enabled enabled state to set
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            toggle();
        }
    }

    /* -------- Getters -------- */

    /**
     * Gets the name of the module.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the description of the module.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the category of the module.
     */
    public Category getCategory() {
        return this.category;
    }

    /**
     * Gets the key of the module.
     */
    public int getKey() {
        return this.keyCode.code;
    }

    /* -------- Setters -------- */

    /**
     * Sets the key of the module.
     *
     * @param key key to set
     */
    public void setKey(int key) {
        this.keyCode.code = key;
    }

    /**
     * Module categories.
     */
    public enum Category {
        COMBAT,
        MOVEMENT,
        RENDER,
        PLAYER,
        MISC
    }
}