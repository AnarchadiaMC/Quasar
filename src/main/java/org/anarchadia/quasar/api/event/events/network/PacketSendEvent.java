package org.anarchadia.quasar.api.event.events.network;

import net.minecraft.network.packet.Packet;
import org.anarchadia.quasar.api.event.EventCancellable;

public class PacketSendEvent extends EventCancellable {
    private Packet<?> packet;

    public PacketSendEvent(EventStage stage, Packet<?> packet) {
        super(stage);
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}