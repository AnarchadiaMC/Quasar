/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.modules;

import io.github.vialdevelopment.attendance.attender.Attender;
import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.RenderInGameHudEvent;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.settings.BooleanSetting;
import org.anarchadia.quasar.api.setting.settings.StringSetting;
import org.anarchadia.quasar.api.util.TPSUtil;
import org.lwjgl.glfw.GLFW;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class Hud extends Module {
    public final BooleanSetting watermark = new BooleanSetting("Watermark", "Renders the Quasar watermark.", true);
    public final StringSetting watermarkText = new StringSetting("Watermark Text", "The text of the watermark.");
    public final BooleanSetting arraylist = new BooleanSetting("Arraylist", "Renders the Quasar arraylist.", true);
    public final BooleanSetting ticks = new BooleanSetting("TPS", "Renders the ticks per second.", true);
    public final BooleanSetting fps = new BooleanSetting("FPS", "Renders the frames per second.", true);

    public Hud() {
        super("Hud", "Renders the Quasar hud.", GLFW.GLFW_KEY_UNKNOWN, Category.RENDER);
        this.addSettings(watermark, watermarkText, arraylist, ticks, fps);
    }

    @Listener
    public void onRenderHudEvent(RenderInGameHudEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (mc.getDebugHud().shouldShowDebugHud()) return;

        // TODO rewrite the whole hud thingy

        if (watermark.isEnabled()) {
            event.getContext().drawTextWithShadow(mc.textRenderer, watermarkText.getString() == null ? Quasar.MOD_NAME : watermarkText.getString() + " v" + Quasar.MOD_VERSION,
                    2, 2, 0xFFFFFF);
        }

        if (fps.isEnabled()) {
            String fps = mc.fpsDebugString.split(" ")[0];
            int x = 2;

            if (watermark.isEnabled()) x = (watermarkText.getString().length() * 15);

            event.getContext().drawTextWithShadow(mc.textRenderer, "(" + fps + " fps)", x, 2, 0xFFFFFF);
        }

        int screenWidth = mc.getWindow().getScaledWidth();
        if (ticks.isEnabled()) {
            String tpsInfo = "Ticks per sec: " + TPSUtil.INSTANCE.getTPS();
            int tpsInfoWidth = mc.textRenderer.getWidth(tpsInfo);
            event.getContext().drawTextWithShadow(mc.textRenderer, tpsInfo, screenWidth - tpsInfoWidth - 2, 2, 0xFFFFFF);
        }

        int y = 5;

        if (arraylist.isEnabled()) {
            for (Module module : Quasar.getInstance().getModuleManager().getEnabledModules()) {
                event.getContext().drawTextWithShadow(mc.textRenderer, ">" + module.name, 2, y += 10, 0xFFFFFF);
            }
        }

    }
}
