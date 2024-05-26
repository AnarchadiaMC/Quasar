package org.anarchadia.quasar.mixin;

import org.anarchadia.quasar.gui.QuasarGui;
import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.event.events.RenderInGameHudEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    private void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        RenderInGameHudEvent event = new RenderInGameHudEvent(context);
        Quasar.getInstance().getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        // Ensure ImGui is rendered on top
        if (QuasarGui.isOpen) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.currentScreen instanceof QuasarGui) {
                ((QuasarGui) client.currentScreen).renderImGui();
            }
        }
    }
}