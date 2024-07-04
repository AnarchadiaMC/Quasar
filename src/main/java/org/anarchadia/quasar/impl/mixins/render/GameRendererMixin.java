package org.anarchadia.quasar.impl.mixins.render;

import org.anarchadia.quasar.impl.gui.QuasarGUI;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.anarchadia.quasar.Quasar.mc;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        // Ensure ImGui rendering happens last
        if (mc.currentScreen instanceof QuasarGUI) {
            ((QuasarGUI) mc.currentScreen).renderGUI();
        }
    }
}