/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar;

import org.anarchadia.quasar.command.CommandManager;
import org.anarchadia.quasar.config.ConfigManager;
import org.anarchadia.quasar.eventbus.EventBus;
import org.anarchadia.quasar.module.ModuleManager;
import org.anarchadia.quasar.setting.SettingManager;
import org.anarchadia.quasar.util.QuasarLogger;
import org.anarchadia.quasar.util.TPSUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Main class for quasar.
 */
public class Quasar implements ModInitializer {
    public static final String MOD_NAME = "Quasar";
    public static final String MOD_VERSION = "1.0";
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Quasar INSTANCE;
    private final EventBus EVENT_BUS = new EventBus();
    private final ModuleManager MODULE_MANAGER = new ModuleManager();
    private final CommandManager COMMAND_MANAGER = new CommandManager();
    private final SettingManager SETTING_MANAGER = new SettingManager();
    private final ConfigManager CONFIG_MANAGER = new ConfigManager();

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
        QuasarLogger.logger.info(MOD_NAME + " v" + MOD_VERSION + " (phase 1) has initialized!");
        CONFIG_MANAGER.load();
        QuasarLogger.logger.info("Loaded config!");

        // Save configs on shutdown
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CONFIG_MANAGER.save();
            QuasarLogger.logger.info("Saved config!");
        });
    }

    /**
     * Called when Minecraft has finished loading.
     *
     * @see org.anarchadia.quasar.mixin.MinecraftClientMixin
     */
    public void postInitialize() {
        EVENT_BUS.register(TPSUtil.INSTANCE);
        QuasarLogger.logger.info("Registered TickRateUtil!");
        QuasarLogger.logger.info(MOD_NAME + " v" + MOD_VERSION + " (phase 2) has initialized!");
    }

    /**
     * Gets the event bus.
     */
    public EventBus getEventBus() {
        return EVENT_BUS;
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
