package org.anarchadia.quasar.impl.mixins;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.RenderInGameHudEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    private void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        RenderInGameHudEvent event = new RenderInGameHudEvent(context);
        Quasar.getInstance().getEventManager().dispatch(event);

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }
    }
}