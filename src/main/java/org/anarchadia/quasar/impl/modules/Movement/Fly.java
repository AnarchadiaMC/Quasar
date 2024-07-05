/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.modules.Movement;

import org.anarchadia.quasar.api.event.events.client.TickEvent;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class Fly extends Module {
    public final Setting<Float> speed = new Setting<Float>("Speed", "How fast to fly.", 3f, 0.1f, 10f, 0.1f);
    private int antiKickTimer = 0;

    public Fly() {
        super("Fly", "Allows you to fly.", GLFW.GLFW_KEY_F, Category.MOVEMENT);
    }


    @Override
    public void onDisable() {
        if (mc.world == null || mc.player == null) return;
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
        mc.player.getAbilities().setFlySpeed(0.05f);
    }

    @Listener
    public void onTickEvent(TickEvent event) {
        if (mc.world == null || mc.player == null) return;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue() / 10f);

        antiKickTimer++;
        if (antiKickTimer > 20 && mc.player.getWorld().getBlockState(BlockPos.ofFloored(mc.player.getPos().subtract(0, 0.0433D, 0))).isAir()) {
            antiKickTimer = 0;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0433D, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.0433D, mc.player.getZ(), true));
        }
    }
}
