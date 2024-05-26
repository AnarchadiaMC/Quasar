/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.api.event.events;

import net.minecraft.network.packet.Packet;
import org.anarchadia.quasar.api.event.EventCancellable;

@SuppressWarnings("rawtypes")
public class PacketEvent extends EventCancellable {
    private final Packet packet;
    private final Type type;

    public PacketEvent(Packet packet, Type type) {
        super();
        this.packet = packet;
        this.type = type;
    }

    /**
     * Returns the packet
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Returns the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Event types
     */
    public enum Type {
        SEND,
        RECEIVE
    }
}
