package org.anarchadia.quasar.mixin;

import org.anarchadia.quasar.gui.QuasarGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        // Ensure ImGui rendering happens last
        if (QuasarGui.isOpen) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.currentScreen instanceof QuasarGui) {
                ((QuasarGui) client.currentScreen).renderImGui();
            }
        }
    }
}