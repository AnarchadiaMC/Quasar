/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.modules.Render;

import org.anarchadia.quasar.api.event.events.render.RenderEvent;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import org.anarchadia.quasar.api.util.LoggingUtil;
import org.anarchadia.quasar.api.util.RenderUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;

public class RenderTest extends Module {
    public final Setting<Boolean> box3D = new Setting<Boolean>("3DBox", "Draws an 3D box around the entity.", false);
    public final Setting<Boolean> boxOutline = new Setting<Boolean>("OutlineBox", "Draws an outline box around the entity.", false);
    public final Setting<Boolean> outline2D = new Setting<Boolean>("2DOutline", "Draws an 2D outline around the entity.", false);
    public final Setting<Boolean> line = new Setting<Boolean>("Line", "Draws an line from player.", false);


    public RenderTest() {
        super("RenderTest", "Shows different types of rendering methods in RenderUtil.", GLFW.GLFW_KEY_UNKNOWN, Category.RENDER);
    }


    @Override
    public void onEnable() {

        LoggingUtil.info("RenderTest will be rendering on the player. Change to third person view, to see them correctly.");
    }

    @Listener
    public void onRenderEvent(RenderEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (box3D.isEnabled()) {
            RenderUtil.draw3DBox(event.getMatrixStack(), RenderUtil.smoothen(mc.player, mc.player.getBoundingBox()), new Color(255, 0, 0, 255));
        } else if (boxOutline.isEnabled()) {
            RenderUtil.drawOutlineBox(event.getMatrixStack(), RenderUtil.smoothen(mc.player, mc.player.getBoundingBox()), new Color(255, 0, 0, 255));
        } else if (outline2D.isEnabled()) {
            RenderUtil.draw2DOutline(event.getMatrixStack(), mc.player, new Color(255, 0, 0, 255));
        } else if (line.isEnabled()) {
            Camera camera = mc.gameRenderer.getCamera();
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof AnimalEntity) {
                    Vec3d start = new Vec3d(0, 0, 1)
                            .rotateX(-(float) Math.toRadians(camera.getPitch()))
                            .rotateY(-(float) Math.toRadians(camera.getYaw()));
                    Vec3d end = RenderUtil.smoothen(entity).add(0, entity.getStandingEyeHeight(), 0);
                    RenderUtil.draw3DLineFromPlayer(event.getMatrixStack(), start, end, new Color(255, 0, 0, 255));
                }
            }
        }
    }
}
