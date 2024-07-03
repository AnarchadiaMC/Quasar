package org.anarchadia.quasar.impl.gui.tabs;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.impl.gui.QuasarGUI;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class ModuleTabs {
    private static final HashMap<Module, ImBoolean> enabledMap = new HashMap<>();
    private static final HashMap<Setting<?>, Object> settingsMap = new HashMap<>();
    private static final HashMap<Module.Category, Boolean> categoryMap = new HashMap<>();
    private static final HashMap<Module, Boolean> showSettingsMap = new HashMap<>();

    /**
     * Renders the module tabs.
     */
    public static void render() {
        for (Module module : Quasar.getInstance().getModuleManager().modules) {
            showSettingsMap.put(module, showSettingsMap.getOrDefault(module, false));
            enabledMap.put(module, new ImBoolean(module.isEnabled()));

            for (Module.Category category : Module.Category.values()) {
                categoryMap.put(category, false);
            }

            for (Setting<?> setting : module.settings) {
                if (setting.getValue() instanceof Boolean) {
                    settingsMap.put(setting, new ImBoolean((Boolean) setting.getValue()));
                } else if (setting.getValue() instanceof Number) {
                    settingsMap.put(setting, new float[]{((Number) setting.getValue()).floatValue()});
                } else if (setting.getValue() instanceof Enum) {
                    settingsMap.put(setting, new ImInt(((Enum<?>) setting.getValue()).ordinal()));
                } else if (setting.getValue() instanceof String) {
                    settingsMap.put(setting, new ImString((String) setting.getValue()));
                } else if (setting.getValue() instanceof Integer && ((Integer) setting.getValue()) >= 0) {
                    settingsMap.put(setting, new ImInt((Integer) setting.getValue()));
                } else {
                    LoggingUtil.logger.warn("Unsupported setting type: " + setting.getValue().getClass().getSimpleName());
                }
            }
        }

        for (Module.Category category : Module.Category.values()) {
            if (categoryMap.get(category)) {
                continue;
            }

            ImGui.begin(category.name(), ImGuiWindowFlags.NoResize);
            if (!categoryMap.get(category)) {
                ImGui.setWindowSize(250 * QuasarGUI.guiWidth.get(), 300 * QuasarGUI.guiHeight.get());
                categoryMap.put(category, true);
            }

            renderCategoryModules(category);

            ImGui.end();
        }
    }

    /**
     * Renders the modules for a given category.
     *
     * @param category The category to render modules for
     */
    private static void renderCategoryModules(Module.Category category) {
        for (Module module : Quasar.getInstance().getModuleManager().getModulesByCategory(category)) {
            ImGui.checkbox(module.getName(), enabledMap.get(module));
            if (ImGui.isItemClicked(1)) {
                showSettingsMap.put(module, !showSettingsMap.get(module));
            }
            ImGui.sameLine(220);
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip(module.getDescription());
            }
            ImGui.text(((module.settings.isEmpty()) ? "" : (showSettingsMap.get(module)) ? "^" : "v"));

            if (showSettingsMap.get(module)) {
                ImGui.indent();
                renderModuleSettings(module);
                ImGui.unindent();
            }

            if (enabledMap.get(module).get() != module.isEnabled()) {
                module.toggle();
            }
        }
    }

    /**
     * Renders the settings for a given module.
     *
     * @param module The module to render settings for
     */
    private static void renderModuleSettings(Module module) {
        for (Setting<?> setting : module.settings) {
            if (setting.equals(module.keyCode)) {
                if (module.isBinding()) {
                    ImGui.text("Press a key to bind... (ESC to cancel)");
                } else {
                    String name = module.getKey() < 0 ? "NONE"
                            : InputUtil.fromKeyCode(module.getKey(), -1).getLocalizedText().getString();
                    if (ImGui.button("Bind: " + name)) {
                        module.startBinding();
                    }
                }
                continue;
            }

            // other setting types rendering below

            if (setting.getValue() instanceof Boolean) {
                ImGui.checkbox(setting.getName(), (ImBoolean) settingsMap.get(setting));
                if (!setting.getValue().equals(((ImBoolean) settingsMap.get(setting)).get())) {
                    setting.setValue(((ImBoolean) settingsMap.get(setting)).get());
                }
            } else if (setting.getValue() instanceof Number) {
                ImGui.sliderFloat(
                        setting.getName(),
                        (float[]) settingsMap.get(setting),
                        ((Number) setting.getMinimum()).floatValue(),
                        ((Number) setting.getMaximum()).floatValue()
                );
                if (!setting.getValue().equals(((float[]) settingsMap.get(setting))[0])) {
                    setting.setValue(((float[]) settingsMap.get(setting))[0]);
                }
            } else if (setting.getValue() instanceof Enum) {
                Enum<?>[] enumConstants = ((Enum<?>) setting.getValue()).getDeclaringClass().getEnumConstants();
                String[] enumNames = new String[enumConstants.length];
                for (int i = 0; i < enumConstants.length; i++) {
                    enumNames[i] = enumConstants[i].name();
                }
                ImGui.combo(setting.getName(), (ImInt) settingsMap.get(setting), enumNames);
                if (((Enum<?>) setting.getValue()).ordinal() != ((ImInt) settingsMap.get(setting)).get()) {
                    setting.setValue(enumConstants[((ImInt) settingsMap.get(setting)).get()]);
                }
            } else if (setting.getValue() instanceof String) {
                ImGui.inputText(setting.getName(), (ImString) settingsMap.get(setting), ImGuiInputTextFlags.CallbackResize);
                String temp = ((ImString) settingsMap.get(setting)).get();
                if (!temp.equals(setting.getValue())) {
                    setting.setValue(temp);
                }
            } else if (setting.getValue() instanceof Integer) {
                ImGui.inputInt(setting.getName(), (ImInt) settingsMap.get(setting));
                if (!setting.getValue().equals(((ImInt) settingsMap.get(setting)).get())) {
                    setting.setValue(((ImInt) settingsMap.get(setting)).get());
                }
            } else {
                LoggingUtil.logger.warn("Unsupported setting type: " + setting.getValue().getClass().getSimpleName());
            }

            if (ImGui.isItemHovered()) {
                ImGui.setTooltip(setting.getDescription());
            }
        }
    }
}