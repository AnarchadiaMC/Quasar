package org.anarchadia.quasar.api.module;

import net.minecraft.client.MinecraftClient;
import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract Module class representing a functional module in the application.
 */
public abstract class Module {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public String name, description;
    public Setting<Integer> keyCode;
    public Category category;
    public boolean enabled;
    public List<Setting<?>> settings = new ArrayList<>();

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
        this.keyCode = new Setting<>("keyCode", "Key binding for module", key);
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
    public void addSettings(Setting<?>... settings) {
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
     * Toggles the module's state.
     */
    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) onEnable();
        else onDisable();
    }

    /**
     * Checks if the module is enabled.
     *
     * @return true if the module is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the enabled state of the module.
     *
     * @param enabled true to enable the module, false to disable.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            toggle();
        }
    }

    /**
     * Gets a setting by its name.
     *
     * @param name the name of the setting.
     * @return the setting with the specified name, or null if no such setting exists.
     */
    public Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (setting.getName().equals(name)) {
                return setting;
            }
        }
        return null; // Return null if no setting is found with the given name
    }

    /* -------- Getters -------- */

    /**
     * Gets the name of the module.
     *
     * @return the module name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the description of the module.
     *
     * @return the module description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the settings of the module.
     *
     * @return the list of settings.
     */
    public List<Setting<?>> getSettings() {
        return settings;
    }

    /**
     * Gets the category of the module.
     *
     * @return the module category.
     */
    public Category getCategory() {
        return this.category;
    }

    /**
     * Gets the key code of the module.
     *
     * @return the key code.
     */
    public int getKey() {
        return this.keyCode.getValue();
    }

    /* -------- Setters -------- */

    /**
     * Sets the key code of the module.
     *
     * @param key the key code to set.
     */
    public void setKey(int key) {
        this.keyCode.setValue(key);
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