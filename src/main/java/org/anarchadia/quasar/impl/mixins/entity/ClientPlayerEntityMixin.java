/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.mixins.entity;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.client.TickEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import org.anarchadia.quasar.api.event.events.entity.PlayerMoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (Quasar.mc.player != null && Quasar.mc.world != null) {
            TickEvent event = new TickEvent();
            Quasar.getInstance().getEventManager().dispatchEvent(event);
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    private void tickMovementHead(CallbackInfo ci) {
        PlayerMoveEvent event = new PlayerMoveEvent();
        Quasar.getInstance().getEventManager().dispatchEvent(event);
        if(event.isCanceled()) ci.cancel();
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void tickMovementTail(CallbackInfo ci) {
        PlayerMoveEvent event = new PlayerMoveEvent();
        Quasar.getInstance().getEventManager().dispatchEvent(event);
    }
}
