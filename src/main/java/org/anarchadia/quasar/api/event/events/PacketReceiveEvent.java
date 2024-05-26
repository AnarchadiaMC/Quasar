package org.anarchadia.quasar.api.event.events;

import net.minecraft.network.packet.Packet;
import org.anarchadia.quasar.api.event.EventCancellable;

public class PacketReceiveEvent extends EventCancellable {
    private Packet<?> packet;

    public PacketReceiveEvent(EventStage stage, Packet<?> packet) {
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