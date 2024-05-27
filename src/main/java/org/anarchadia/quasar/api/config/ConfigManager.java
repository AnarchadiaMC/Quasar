package org.anarchadia.quasar.api.config;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.setting.settings.BooleanSetting;
import org.anarchadia.quasar.api.setting.settings.ModeSetting;
import org.anarchadia.quasar.api.setting.settings.NumberSetting;
import org.anarchadia.quasar.api.setting.settings.StringSetting;
import org.anarchadia.quasar.api.util.QuasarLogger;
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

    public File getFile() {
        return file;
    }

    public File getMainDirectory() {
        return mainDirectory;
    }

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

    private void processSettings(Properties properties, boolean save) {
        for (Module module : Quasar.getInstance().getModuleManager().getModules()) {
            String propertyName = module.getName() + ".enabled";
            if (save) {
                properties.setProperty(propertyName, String.valueOf(module.isEnabled()));
            } else {
                module.setEnabled(Boolean.parseBoolean(properties.getProperty(propertyName, "false")));
            }

            for (Setting setting : module.settings) {
                processEachSetting(save, properties, setting, module);
            }
        }
    }

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

    private void processBooleanSetting(boolean save, Properties properties, BooleanSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, String.valueOf(setting.isEnabled()));
        } else {
            setting.setEnabled(Boolean.parseBoolean(properties.getProperty(propertyName, "false")));
        }
    }

    private void processNumberSetting(boolean save, Properties properties, NumberSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, String.valueOf(setting.getValue()));
        } else {
            setting.setValue(Double.parseDouble(properties.getProperty(propertyName, "0")));
        }
    }

    private void processStringSetting(boolean save, Properties properties, StringSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, setting.getString());
        } else {
            setting.setString(properties.getProperty(propertyName, ""));
        }
    }

    private void processModeSetting(boolean save, Properties properties, ModeSetting setting, Module module) {
        String propertyName = module.getName() + "." + setting.getName();
        if (save) {
            properties.setProperty(propertyName, setting.getMode());
        } else {
            String mode = properties.getProperty(propertyName);
            if (mode != null) {
                setting.setMode(mode);
            } else {
                properties.setProperty(propertyName, setting.getMode()); // Save current mode if not present
            }
        }
    }

    private void processKeybindSetting(boolean save, Properties properties, Module module) {
        String propertyName = module.getName() + ".key";
        if (save) {
            properties.setProperty(propertyName, String.valueOf(module.getKey()));
        } else {
            String keyProperty = properties.getProperty(propertyName);
            if (keyProperty != null) {
                module.setKey(Integer.parseInt(keyProperty));
            } else {
                properties.setProperty(propertyName, String.valueOf(module.getKey())); // Save default key if not present
            }
        }
    }
}