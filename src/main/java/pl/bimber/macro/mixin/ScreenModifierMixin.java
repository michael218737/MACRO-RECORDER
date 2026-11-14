package pl.bimber.macro.mixin;

import pl.bimber.macro.PlaybackModifiers;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenModifierMixin {
    @Inject(method = "hasShiftDown", at = @At("HEAD"), cancellable = true)
    private static void macroRecorder$shift(CallbackInfoReturnable<Boolean> cir) {
        if (PlaybackModifiers.isActive()) cir.setReturnValue(PlaybackModifiers.shiftDown());
    }

    @Inject(method = "hasControlDown", at = @At("HEAD"), cancellable = true)
    private static void macroRecorder$control(CallbackInfoReturnable<Boolean> cir) {
        if (PlaybackModifiers.isActive()) cir.setReturnValue(PlaybackModifiers.controlDown());
    }

    @Inject(method = "hasAltDown", at = @At("HEAD"), cancellable = true)
    private static void macroRecorder$alt(CallbackInfoReturnable<Boolean> cir) {
        if (PlaybackModifiers.isActive()) cir.setReturnValue(PlaybackModifiers.altDown());
    }
}
