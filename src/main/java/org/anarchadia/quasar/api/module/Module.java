package org.anarchadia.quasar.api.module;

import net.minecraft.client.MinecraftClient;
import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.setting.settings.KeybindSetting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class Module {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public String name, description;
    public KeybindSetting keyCode;
    public Category category;
    public boolean enabled;
    public List<Setting> settings = new ArrayList<>();

    /**
     * Module constructor.
     *
     * @param name        name of the module.
     * @param description description of the module.
     * @param key         GLFW key binding.
     * @param category    module category.
     */
    public Module(String name, String description, int key, Category category) {
        super();
        this.name = name;
        this.description = description;
        this.keyCode = new KeybindSetting(key); // Ensure the key is initialized here
        this.category = category;
        this.enabled = false; // Ensure default value

        /* Add default settings */
        addSettings(keyCode);
    }

    /**
     * Adds settings to the module. Must be called in the constructor.
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

        LoggingUtil.info(Formatting.GREEN + "Enabled " + this.getName() + "!");
    }

    /**
     * Called when the module is disabled.
     */
    public void onDisable() {
        Quasar.getInstance().getEventManager().removeEventListener(this);
        Quasar.getInstance().getConfigManager().save();

        LoggingUtil.info(Formatting.RED + "Disabled " + this.getName() + "!");
    }

    /**
     * Toggles a module.
     */
    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) onEnable();
        else onDisable();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            toggle();
        }
    }

    /* -------- Getters -------- */

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getKey() {
        return this.keyCode.getKeyCode();
    }

    /* -------- Setters -------- */

    public void setKey(int key) {
        this.keyCode.setKeyCode(key);
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