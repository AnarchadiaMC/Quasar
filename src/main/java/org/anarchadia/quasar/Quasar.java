/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar;

import org.anarchadia.quasar.api.command.CommandManager;
import org.anarchadia.quasar.api.config.ConfigManager;
import org.anarchadia.quasar.api.setting.SettingManager;
import org.anarchadia.quasar.api.util.LoggingUtil;
import org.anarchadia.quasar.api.util.TPSUtil;
import org.anarchadia.quasar.api.module.ModuleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import team.stiff.pomelo.impl.annotated.AnnotatedEventManager;

/**
 * Main class for quasar.
 */
public class Quasar implements ModInitializer {
    public static final String MOD_NAME = "Quasar";
    public static final String MOD_VERSION = "1.0";
    public static MinecraftClient mc = MinecraftClient.getInstance();
    private static Quasar INSTANCE;
    private final ModuleManager MODULE_MANAGER = new ModuleManager();
    private final CommandManager COMMAND_MANAGER = new CommandManager();
    private final SettingManager SETTING_MANAGER = new SettingManager();
    private final ConfigManager CONFIG_MANAGER = new ConfigManager();
    public AnnotatedEventManager EVENT_MANAGER = new AnnotatedEventManager();


    public Quasar() {
        INSTANCE = this;
    }

    /**
     * Gets the instance of Quasar.
     */
    public static Quasar getInstance() {
        return INSTANCE;
    }

    /**
     * Called when quasar is initialized.
     */
    @Override
    public void onInitialize() {
        LoggingUtil.logger.info(MOD_NAME + " v" + MOD_VERSION + " has initialized!");
        try {
            CONFIG_MANAGER.load();
            LoggingUtil.logger.info("Loaded config!");
        } catch (Exception e) {
            LoggingUtil.logger.error("Error loading config: " + e.getMessage(), e);
        }

        // Save configs on shutdown
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try {
                CONFIG_MANAGER.save();
                LoggingUtil.logger.info("Saved config!");
            } catch (Exception e) {
                LoggingUtil.logger.error("Error saving config: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Called when Minecraft has finished loading.
     *
     * @see org.anarchadia.quasar.impl.mixins.MinecraftClientMixin
     */
    public void postInitialize() {
        getEventManager().addEventListener(TPSUtil.INSTANCE);
        LoggingUtil.logger.info("Registered TickRateUtil!");
        LoggingUtil.logger.info(MOD_NAME + " v" + MOD_VERSION + " has posted beyond initialization!");
    }

    /**
     * Gets the event manager
     */
    public AnnotatedEventManager getEventManager() {
        return EVENT_MANAGER;
    }

    /**
     * Gets the module manager.
     */
    public ModuleManager getModuleManager() {
        return MODULE_MANAGER;
    }

    /**
     * Gets the command manager.
     */
    public CommandManager getCommandManager() {
        return COMMAND_MANAGER;
    }

    /**
     * Gets the setting manager.
     */
    public SettingManager getSettingManager() {
        return SETTING_MANAGER;
    }

    /**
     * Gets the config manager.
     */
    public ConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }
}
