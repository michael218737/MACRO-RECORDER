package pl.bimber.macro;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class MacroRecorderClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("macro_recorder", "controls"));

    private static KeyMapping startKey;
    private static KeyMapping stopKey;
    private static KeyMapping playKey;
    private static KeyMapping cancelKey;
    private static final MacroEngine ENGINE = new MacroEngine();

    @Override
    public void onInitializeClient() {
        startKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.macro_recorder.start", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, CATEGORY));
        stopKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.macro_recorder.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, CATEGORY));
        playKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.macro_recorder.play", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, CATEGORY));
        cancelKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.macro_recorder.cancel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, CATEGORY));

        ClientSendMessageEvents.COMMAND.register(ENGINE::recordCommand);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (startKey.consumeClick()) ENGINE.startRecording(client);
            while (stopKey.consumeClick()) ENGINE.stopRecording(client);
            while (playKey.consumeClick()) ENGINE.startPlayback(client);
            while (cancelKey.consumeClick()) ENGINE.stopPlayback(client);
            ENGINE.tick(client);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("macro_recorder", "macro_status"),
                (graphics, tickCounter) -> renderHud(graphics));
    }

    private static void renderHud(GuiGraphics graphics) {
        if (!ENGINE.shouldShowHud()) return;
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int x = width - 164;
        int y = 8;
        int bg = 0xCC10101A;
        int accent = ENGINE.isRecording() ? 0xFFFF4D6D : 0xFF9B7BFF;
        graphics.fill(x, y, width - 8, y + 62, bg);
        graphics.fill(x, y, x + 3, y + 62, accent);
        graphics.drawString(client.font, "MACRO RECORDER", x + 10, y + 7, 0xFFFFFFFF, false);
        graphics.drawString(client.font, ENGINE.statusText(), x + 10, y + 20, accent, false);
        graphics.drawString(client.font, "REC F6  STOP F7", x + 10, y + 34, 0xFFE0E0EA, false);
        graphics.drawString(client.font, "PLAY F8  CANCEL F9", x + 10, y + 47, 0xFFE0E0EA, false);
    }
}
