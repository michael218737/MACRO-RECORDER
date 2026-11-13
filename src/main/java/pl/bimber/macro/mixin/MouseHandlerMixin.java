package pl.bimber.macro.mixin;

import pl.bimber.macro.MacroEngine;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void macroRecorder$captureMouse(long window, MouseInput input, int action, CallbackInfo ci) {
        int button = input.button();
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        MacroEngine.captureMouseEvent(button, action, x[0], y[0]);
    }
}
