package org.anarchadia.quasar.impl.modules.Movement;

import org.anarchadia.quasar.api.event.events.network.PacketSendEvent;
import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class ElytraFlight extends Module {

    public final Setting<Mode> flightMode = new Setting<>(
            "Mode",
            "Specify which Elytra Flight mode you'd like to use",
            Mode.Packet
    );

    public final Setting<Float> initialSpeed = new Setting<>(
            "Starting Speed",
            "The speed your elytra flight will start at.",
            1.0f,
            0.1f,
            10.0f,
            0.1f
    ).setVisibilityCondition(ret -> flightMode.getValue().equals(Mode.Packet));

    public final Setting<Float> incrementalSpeed = new Setting<>(
            "Incremental Speed",
            "The speed you will increase via Incremental Ticks.",
            1.0f,
            0.1f,
            10.0f,
            0.1f
    ).setVisibilityCondition(ret -> flightMode.getValue().equals(Mode.Packet));

    public final Setting<Float> maximumSpeed = new Setting<>(
            "Maximum Speed",
            "Caps the maximum speed you you will reach using an elytra.",
            1.0f,
            1f,
            10.0f,
            0.25f
    ).setVisibilityCondition(ret -> flightMode.getValue().equals(Mode.Packet));

    public final Setting<Integer> incrementalTicks = new Setting<>(
            "Incremental Ticks",
            "The number of ticks before incrementing your speed.",
            3,
            1,
            20,
            1
    ).setVisibilityCondition(ret -> flightMode.getValue().equals(Mode.Packet));

    public final Setting<Float> timerSpeed = new Setting<>(
            "Timer Speed",
            "Multiplies your in-game Tick Speed by the set value.",
            1f,
            0.01f,
            10.0f,
            0.1f
    );

    /**
     * Module constructor.
     */
    public ElytraFlight() {
        super(
                "ElytraFlight",
                "Enables various elytra options, settings and modifications.",
                -1,
                Category.MOVEMENT
        );
    }

    @Listener
    public void onPacketSend(PacketSendEvent event) {
        switch (event.getStage()) {

        }
    }

    public enum Mode {
        Packet,
        Vanilla
    }
}
