package org.anarchadia.quasar.impl.modules.Movement;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.anarchadia.quasar.api.event.EventStageable;
import org.anarchadia.quasar.api.event.events.entity.PlayerMoveEvent;
import org.anarchadia.quasar.api.event.events.network.PacketReceiveEvent;
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

    public final Setting<Boolean> infDurability = new Setting<>(
            "Infinite Durability",
            "Increases your maximum durability.",
            true
    ).setVisibilityCondition(ret -> flightMode.getValue().equals(Mode.Packet));

    public enum Mode {
        Packet,
        Vanilla
    }

    private float acceleration;
    private int tickCounter;

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

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().setFlySpeed(0.05F);
    }

    @Listener
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket && flightMode.getValue() == Mode.Packet) {
            if ((!isBoxCollidingGround()) && mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
                if (infDurability.getValue() || !mc.player.isFallFlying()) {
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
        }
    }

    @Listener
    public void onPlayerMove(PlayerMoveEvent event) {
        ClientPlayerEntity player = mc.player;
        if (flightMode.getValue() == Mode.Packet && event.getStage() == EventStageable.EventStage.PRE) {
            // Movement adjustments for Elytra Fly
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().setFlySpeed(0.05F);

            if (mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA)
                return;

            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed((initialSpeed.getValue() / 15f) * Math.min((acceleration += incrementalSpeed.getValue()) / 100.0f, maximumSpeed.getValue()));

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            if (tickCounter++ % incrementalTicks.getValue() == 0) {

                y = 0;

                if (!isMoving() && Math.abs(x) < 0.121 && Math.abs(z) < 0.121) {
                    float angleToRad = (float) Math.toRadians(4.5 * (mc.player.age % 80));
                    x = Math.sin(angleToRad) * 0.12;
                    z = Math.cos(angleToRad) * 0.12;
                }

                mc.player.setPosition(x, y, z);
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof EntityTrackerUpdateS2CPacket packet && packet.id() == mc.player.getId()) {
            for (DataTracker.SerializedEntry<?> value : packet.trackedValues())
                if (value.id() == 0 && (value.value().toString().equals("-120") || value.value().toString().equals("-128") || value.value().toString().equals("-126")))
                    event.setCanceled(true);
        }

        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            acceleration = 0;
            this.toggle();
        }
    }

    private void reset() {
        acceleration = 0;
        tickCounter = 0;
    }

    private boolean isBoxCollidingGround() {
        return mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, -0.3, 0.0)).iterator().hasNext();
    }

    public static boolean isMoving() {
        return mc.player != null && mc.world != null && mc.player.input != null && (mc.player.input.movementForward != 0.0 || mc.player.input.movementSideways != 0.0);
    }
}
