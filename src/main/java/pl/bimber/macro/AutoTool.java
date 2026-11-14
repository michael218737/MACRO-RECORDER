package pl.bimber.macro;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AutoTool {
    private static boolean enabled;

    private AutoTool() {}

    public static void load() {
        Path file = configFile();
        Properties properties = new Properties();
        if (Files.exists(file)) {
            try (var reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            } catch (IOException ignored) {
            }
        }
        enabled = Boolean.parseBoolean(properties.getProperty("autotool", "false"));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        Properties properties = new Properties();
        properties.setProperty("autotool", Boolean.toString(enabled));
        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (var writer = Files.newBufferedWriter(file)) {
                properties.store(writer, "Macro Recorder settings");
            }
        } catch (IOException ignored) {
        }
    }

    public static void tick(Minecraft client) {
        if (!enabled || client.player == null || client.level == null || client.screen != null
                || !client.options.keyAttack.isDown() || !(client.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return;

        var state = client.level.getBlockState(hit.getBlockPos());
        var inventory = client.player.getInventory();
        int bestSlot = inventory.getSelectedSlot();
        float bestSpeed = inventory.getItem(bestSlot).getDestroySpeed(state);
        for (int slot = 0; slot < 9; slot++) {
            float speed = inventory.getItem(slot).getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }
        inventory.selected = bestSlot;
    }

    private static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("macro-recorder.properties");
    }
}
