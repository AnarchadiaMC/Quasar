/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.impl.mixins;

import org.anarchadia.quasar.Quasar;
import org.anarchadia.quasar.api.event.events.KeyEvent;
import org.anarchadia.quasar.api.module.Module;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;isKeyPressed(JI)Z", ordinal = 2), method = "onKey", cancellable = true)
    private void onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (Quasar.mc.currentScreen != null) return;
        if (action == 2) action = 1;
        switch (action) {
            case 0 -> {
                KeyEvent event = new KeyEvent(key, scanCode, KeyEvent.Status.RELEASED);
                Quasar.getInstance().getEventManager().dispatchEvent(event);
                if (event.isCanceled()) ci.cancel();
            }
            case 1 -> {
                KeyEvent event = new KeyEvent(key, scanCode, KeyEvent.Status.PRESSED);
                Quasar.getInstance().getModuleManager().getModules().stream().filter(m -> m.getKey() == key).forEach(Module::toggle);
                Quasar.getInstance().getEventManager().dispatchEvent(event);
                if (event.isCanceled()) ci.cancel();
            }
        }
    }
}
