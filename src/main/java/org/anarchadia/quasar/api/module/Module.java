package org.anarchadia.quasar.api.module;

import net.minecraft.client.MinecraftClient;
import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
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
    protected boolean enabled;
    public List<Setting<?>> settings = new ArrayList<>();
    private boolean settingsRegistered = false;

    /**
     * Module constructor.
     *
     * @param name        name of the module.
     * @param description description of the module.
     * @param key         GLFW key binding.
     * @param category    module category.
     */
    public Module(String name, String description, int key, Category category) {
        this.name = name;
        this.description = description;
        this.keyCode = new Setting<>("keyCode", "Key binding for module", key);
        this.category = category;
        this.enabled = false;

        /* Add default settings */
        addSettings(keyCode);

        // Register the settings after the subclass constructor has completed
        registerSettings();
    }

    /**
     * Registers all `Setting` fields in the module using reflection.
     */
    protected void registerSettings() {
        if (settingsRegistered) return;

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Setting<?> setting = (Setting<?>) field.get(this);
                    if (setting != null && !settings.contains(setting)) {
                        addSettings(setting);
                    }
                } catch (IllegalAccessException e) {
                    System.out.println("Failed to access field: " + field.getName() + " - " + e.getMessage());
                }
            }
        }
        settings.sort(Comparator.comparingInt(s -> s == keyCode ? 0 : 1));
        settingsRegistered = true;
    }

    public void startBinding() {
        Quasar.getInstance().getModuleManager().setCurrentlyBindingModule(this);
    }

    public boolean isBinding() {
        return Quasar.getInstance().getModuleManager().getCurrentlyBindingModule() == this;
    }

    /**
     * Adds settings to the module.
     *
     * @param settings settings to add
     */
    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
        this.settings.sort(Comparator.comparingInt(s -> s == keyCode ? 1 : 0));
    }

    /**
     * Toggles the module's state.
     */
    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Sets the enabled state of the module.
     *
     * @param enabled true to enable the module, false to disable.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                enable();
            } else {
                disable();
            }
            Quasar.getInstance().getConfigManager().save();
        }
    }

    /**
     * Enables the module.
     */
    private void enable() {
        onEnable();
        Quasar.getInstance().getEventManager().addEventListener(this);
        LoggingUtil.info(Formatting.GREEN + "Enabled " + this.getName() + "!");
    }

    /**
     * Disables the module.
     */
    private void disable() {
        onDisable();
        Quasar.getInstance().getEventManager().removeEventListener(this);
        LoggingUtil.info(Formatting.RED + "Disabled " + this.getName() + "!");
    }

    /**
     * Called when the module is enabled. Subclasses can override this method.
     */
    protected void onEnable() {}

    /**
     * Called when the module is disabled. Subclasses can override this method.
     */
    protected void onDisable() {}

    /**
     * Checks if the module is enabled.
     *
     * @return true if the module is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return this.enabled;
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
        if (!settingsRegistered) {
            registerSettings();
        }
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

    /**
     * Sets the key code of the module.
     *
     * @param key the key code to set.
     */
    public synchronized void setKey(int key) {
        this.keyCode.setValue(key);
        Quasar.getInstance().getConfigManager().save();
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