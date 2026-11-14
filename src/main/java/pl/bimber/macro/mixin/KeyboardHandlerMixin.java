package pl.bimber.macro.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.bimber.macro.MacroEngine;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void macroRecorder$captureKey(long window, int action, KeyEvent input, CallbackInfo ci) {
        MacroEngine.captureKeyEvent(input.key(), input.scancode(), input.modifiers(), action, Minecraft.getInstance().screen != null);
    }
}
