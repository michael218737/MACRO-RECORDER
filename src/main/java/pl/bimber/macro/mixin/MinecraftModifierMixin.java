package pl.bimber.macro.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.bimber.macro.PlaybackModifiers;

@Mixin(Minecraft.class)
public abstract class MinecraftModifierMixin {
    @Inject(method = "hasShiftDown", at = @At("HEAD"), cancellable = true)
    private void macroRecorder$shiftDown(CallbackInfoReturnable<Boolean> cir) {
        if (PlaybackModifiers.isActive()) cir.setReturnValue(PlaybackModifiers.shiftDown());
    }

    @Inject(method = "hasControlDown", at = @At("HEAD"), cancellable = true)
    private void macroRecorder$controlDown(CallbackInfoReturnable<Boolean> cir) {
        if (PlaybackModifiers.isActive()) cir.setReturnValue(PlaybackModifiers.controlDown());
    }

    @Inject(method = "hasAltDown", at = @At("HEAD"), cancellable = true)
    private void macroRecorder$altDown(CallbackInfoReturnable<Boolean> cir) {
        if (PlaybackModifiers.isActive()) cir.setReturnValue(PlaybackModifiers.altDown());
    }
}
