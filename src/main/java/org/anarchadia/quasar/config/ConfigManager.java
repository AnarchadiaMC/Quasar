/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.config;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.module.Module;
import org.anarchadia.quasar.setting.Setting;
import org.anarchadia.quasar.setting.settings.BooleanSetting;
import org.anarchadia.quasar.setting.settings.ModeSetting;
import org.anarchadia.quasar.setting.settings.NumberSetting;
import org.anarchadia.quasar.setting.settings.StringSetting;
import org.anarchadia.quasar.util.QuasarLogger;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConfigManager {
    private final File file;
    private final File mainDirectory;

    public ConfigManager() {
        mainDirectory = new File(MinecraftClient.getInstance().runDirectory, "quasar");

        if (!mainDirectory.exists()) {
            mainDirectory.mkdir();
        }

        file = new File(mainDirectory, "config.xml");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            QuasarLogger.logger.error(e.getMessage());
        }
    }

    /**
     * Gets the saved config file.
     *
     * @return config file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the main directory.
     *
     * @return main dir.
     */
    public File getMainDirectory() {
        return mainDirectory;
    }

    /**
     * Saves the config file by processing the settings and storing them into an XML file (config.xml by default).
     */
    public void save() {
        try {
            QuasarLogger.logger.info("Saving config...");
            Properties properties = new Properties();
            processSettings(properties, true);
            properties.storeToXML(new FileOutputStream(file), null);
        } catch (Exception e) {
            QuasarLogger.logger.error("Error while saving config!", e);
        }
    }

    /**
     * Loads the settings and other data from the config file.
     */
    public void load() {
        try {
            QuasarLogger.logger.info("Loading config...");
            Properties properties = new Properties();
            properties.loadFromXML(new FileInputStream(file));
            processSettings(properties, false);
        } catch (Exception e) {
            QuasarLogger.logger.error("Error while loading config!", e);
        }
    }

    /**
     * Processes the config.
     *
     * @param properties setting property.
     * @param save should the settings be saved or loaded (true for saving, false for loading).
     */
    private void processSettings(Properties properties, boolean save) {
        for (Module module : Quasar.getInstance().getModuleManager().getModules()) {
            String propertyName = module.getName() + ".enabled";
            if (save) {
                properties.setProperty(propertyName,  String.valueOf(module.isEnabled()));
            } else {
                module.setEnabled(Boolean.parseBoolean(properties.getProperty(propertyName)));
            }

            for (Setting setting : module.settings) {
                processEachSetting(save, properties, setting, module);
            }
        }
    }

    /**
     * Processes each modules settings.
     *
     * @param save should the setting be saved or loaded (true for saving, false for loading).
     * @param properties setting properties.
     * @param setting name of the setting.
     * @param module module of the settings.
     */
    private void processEachSetting(boolean save, Properties properties, Setting setting, Module module) {
        String className = setting.getClass().getSimpleName();
        switch (className) {
            case "BooleanSetting":
                processBooleanSetting(save, properties, (BooleanSetting) setting, module);
                break;
            case "NumberSetting":
                processNumberSetting(save, properties, (NumberSetting) setting, module);
                break;
            case "StringSetting":
                processStringSetting(save, properties, (StringSetting) setting, module);
                break;
            case "ModeSetting":
                processModeSetting(save, properties, (ModeSetting) setting, module);
                break;
            case "KeybindSetting":
                processKeybindSetting(save, properties, module);
                break;
            default:
                QuasarLogger.logger.error("Unknown setting type: " + className);
        }
    }

    // Processing of each setting.

    private void processBooleanSetting(boolean save, Properties properties, BooleanSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, String.valueOf(setting.isEnabled()));
        } else {
            setting.setEnabled(Boolean.parseBoolean(properties.getProperty(propertyName)));
        }
    }

    private void processNumberSetting(boolean save, Properties properties, NumberSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, String.valueOf(setting.getValue()));
        } else {
            setting.setValue(Double.parseDouble(properties.getProperty(module.getName() + "." + setting.getName())));
        }
    }

    private void processStringSetting(boolean save, Properties properties, StringSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, setting.getString());
        } else {
            setting.setString(properties.getProperty(module.getName() + "." + setting.getName()));
        }
    }

    private void processModeSetting(boolean save, Properties properties, ModeSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, String.valueOf(setting.getMode()));
        } else {
            setting.setMode(properties.getProperty(module.getName() + "." + setting.getName()));
        }
    }

    private void processKeybindSetting(boolean save, Properties properties, Module module) {
        if (save) {
            properties.setProperty(module.getName() + ".key", String.valueOf(module.getKey()));
        } else {
            String keyProperty = properties.getProperty(module.getName() + ".key");
            if (keyProperty != null) {
                module.setKey(Integer.parseInt(keyProperty));
            }
        }
    }
}
