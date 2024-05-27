/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.util;

import org.anarchadia.quasar.api.event.EventStageable;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import org.anarchadia.quasar.api.event.events.network.PacketReceiveEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class TPSUtil {
    public static TPSUtil INSTANCE = new TPSUtil();
    private static double ticks = 0;
    private static long prevTime = 0;

    @Listener
    public void onPacketReceivedEvent(PacketReceiveEvent event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
                long time = System.currentTimeMillis();
                long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
                ticks = (MathHelper.clamp(20 / (timeOffset / 1000d), 0, 20) * 100d) / 100d;
                prevTime = time;
            }
        }
    }

    /**
     * Returns the ticks per-second.
     *
     * @return ticks
     */
    public double getTPS() {
        // Return TPS with 2 decimal places
        return Math.round(ticks * 100d) / 100d;
    }
}
