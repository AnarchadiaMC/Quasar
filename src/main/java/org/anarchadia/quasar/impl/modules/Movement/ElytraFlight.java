package org.anarchadia.quasar.impl.modules.Movement;

import org.anarchadia.quasar.api.module.Module;
import org.anarchadia.quasar.api.setting.Setting;

public class ElytraFlight extends Module {

    public final Setting<Float> initialSpeed = new Setting<>(
            "Starting Speed",
            "The speed your elytra flight will start at.",
            1.0f,
            0.1f,
            10.0f,
            0.1f
    );

    public final Setting<Float> incrementalSpeed = new Setting<>(
            "Incremental Speed",
            "The speed you will increase via Incremental Ticks.",
            1.0f,
            0.1f,
            10.0f,
            0.1f
    );

    public final Setting<Float> maximumSpeed = new Setting<>(
            "Maximum Speed",
            "Caps the maximum speed you you will reach using an elytra.",
            1.0f,
            1f,
            10.0f,
            0.25f
    );

    public final Setting<Integer> incrementalTicks = new Setting<>(
            "Incremental Ticks",
            "The number of ticks before incrementing your speed.",
            3,
            1,
            20,
            1
    );

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


}
