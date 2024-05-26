/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.mixins;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.anarchadia.quasar.api.event.events.PacketReceiveEvent;
import org.anarchadia.quasar.api.event.events.PacketSendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.anarchadia.quasar.api.event.EventStageable.EventStage.POST;
import static org.anarchadia.quasar.api.event.EventStageable.EventStage.PRE;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void packetReceivePre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketReceiveEvent event = new PacketReceiveEvent(PRE, packet);
        Quasar.getInstance().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "channelRead0*", at = @At("TAIL"))
    private void packetReceivePost(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketReceiveEvent event = new PacketReceiveEvent(POST, packet);
        Quasar.getInstance().getEventManager().dispatchEvent(event);
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void packetSend(Packet<?> packet, CallbackInfo ci) {
        /* This is for the client commands */
        if (packet instanceof ChatMessageC2SPacket pack) {
            if (pack.chatMessage().startsWith(Quasar.getInstance().getCommandManager().prefix)) {
                Quasar.getInstance().getCommandManager().execute(pack.chatMessage());
                ci.cancel();
            }
        }

        PacketSendEvent event = new PacketSendEvent(PRE, packet);
        Quasar.getInstance().getEventManager().dispatchEvent(event);
        if(event.isCanceled()) ci.cancel();
    }

}
