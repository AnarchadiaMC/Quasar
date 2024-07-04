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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        Setting<?> keyBindSetting = null;
        List<Setting<?>> otherSettings = new ArrayList<>();

        // Separate key bind setting from other settings
        for (Setting<?> setting : module.getSettings()) {
            if (setting.equals(module.keyCode)) {
                keyBindSetting = setting;
            } else {
                otherSettings.add(setting);
            }
        }

        // Render other settings
        for (Setting<?> setting : otherSettings) {
            if (setting.isVisible()) {
                renderSetting(setting);
            }
        }

        // Render key bind setting last
        if (keyBindSetting != null && keyBindSetting.getValue() instanceof Integer && keyBindSetting.isVisible()) {
            Setting<Integer> keyCodeSetting = (Setting<Integer>) keyBindSetting;
            if (module.isBinding()) {
                ImGui.text("Press a key to bind... (ESC to cancel)");
            } else {
                int keyCode = keyCodeSetting.getValue();
                String name = keyCode < 0 ? "NONE" : InputUtil.fromKeyCode(keyCode, -1).getLocalizedText().getString();
                if (ImGui.button("Bind: " + name)) {
                    module.startBinding();
                }
            }

            // Check if the key has changed
            int currentKey = module.getKey();
            if (currentKey != keyCodeSetting.getValue()) {
                keyCodeSetting.setValue(currentKey);
                Quasar.getInstance().getConfigManager().save();
            }
        }
    }

    private static void renderSetting(Setting<?> setting) {
        if (setting == null || setting.getValue() == null) {
            LoggingUtil.logger.warn("Null setting or setting value encountered");
            return;
        }

        boolean valueChanged = false;
        try {
            String uniqueId = "##" + setting.getName();

            if (setting.getValue() instanceof Boolean) {
                ImBoolean imBool = (ImBoolean) settingsMap.get(setting);
                if (imBool == null) {
                    imBool = new ImBoolean((Boolean) setting.getValue());
                    settingsMap.put(setting, imBool);
                }

                ImGui.checkbox(uniqueId, imBool);
                ImGui.sameLine();
                ImGui.text(setting.getName());

                if (!setting.getValue().equals(imBool.get())) {
                    setting.setValue(imBool.get());
                    valueChanged = true;
                }
            } else if (setting.getValue() instanceof Number) {
                float[] value = (float[]) settingsMap.get(setting);
                if (value == null) {
                    value = new float[]{((Number) setting.getValue()).floatValue()};
                    settingsMap.put(setting, value);
                }

                ImGui.text(setting.getName());
                ImGui.sliderFloat(uniqueId, value, ((Number) setting.getMinimum()).floatValue(), ((Number) setting.getMaximum()).floatValue());
                if (!setting.getValue().equals(value[0])) {
                    setting.setValue(value[0]);
                    valueChanged = true;
                }
            } else if (setting.getValue() instanceof Enum) {
                ImInt imInt = (ImInt) settingsMap.get(setting);
                if (imInt == null) {
                    imInt = new ImInt(((Enum<?>) setting.getValue()).ordinal());
                    settingsMap.put(setting, imInt);
                }

                Enum<?>[] enumConstants = ((Enum<?>) setting.getValue()).getDeclaringClass().getEnumConstants();
                String[] enumNames = new String[enumConstants.length];
                for (int i = 0; i < enumConstants.length; i++) {
                    enumNames[i] = enumConstants[i].name();
                }

                ImGui.text(setting.getName());
                int currentOrdinal = imInt.get();
                if (ImGui.combo(uniqueId, imInt, enumNames)) {
                    if (currentOrdinal != imInt.get()) {
                        Enum<?> newValue = enumConstants[imInt.get()];
                        setting.setValue(newValue);
                        valueChanged = true;
                    }
                }
            } else if (setting.getValue() instanceof String) {
                ImString imString = (ImString) settingsMap.get(setting);
                if (imString == null) {
                    imString = new ImString((String) setting.getValue());
                    settingsMap.put(setting, imString);
                }

                ImGui.text(setting.getName());
                ImGui.inputText(uniqueId, imString, ImGuiInputTextFlags.CallbackResize);
                String temp = imString.get();
                if (!temp.equals(setting.getValue())) {
                    setting.setValue(temp);
                    valueChanged = true;
                }
            } else if (setting.getValue() instanceof Integer) {
                ImInt imInt = (ImInt) settingsMap.get(setting);
                if (imInt == null) {
                    imInt = new ImInt((Integer) setting.getValue());
                    settingsMap.put(setting, imInt);
                }

                ImGui.text(setting.getName());
                ImGui.inputInt(uniqueId, imInt);
                if (!setting.getValue().equals(imInt.get())) {
                    setting.setValue(imInt.get());
                    valueChanged = true;
                }
            } else {
                LoggingUtil.logger.warn("Unsupported setting type: " + setting.getValue().getClass().getSimpleName());
            }

            if (ImGui.isItemHovered()) {
                ImGui.setTooltip(setting.getDescription());
            }

            if (valueChanged) {
                Quasar.getInstance().getConfigManager().save();
            }
        } catch (Exception e) {
            LoggingUtil.logger.error("Error rendering setting: " + setting.getName(), e);
        }
    }

}