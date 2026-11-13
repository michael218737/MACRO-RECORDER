package pl.bimber.macro;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

final class MacroEngine {
    private enum Mode { IDLE, RECORDING, PLAYING }
    private enum Action {
        FORWARD, BACK, LEFT, RIGHT, JUMP, SNEAK, SPRINT, COMMAND,
        MOUSE_MOVE, MOUSE_LEFT, MOUSE_RIGHT, LOOP_END
    }
    private record Event(int tick, Action action, boolean pressed, String value) {}

    private Mode mode = Mode.IDLE;
    private final List<Event> events = new ArrayList<>();
    private int tick;
    private int playbackIndex;
    private boolean[] previous = new boolean[7];
    private final boolean[] playbackHeld = new boolean[7];
    private final boolean[] previousMouse = new boolean[2];
    private final boolean[] playbackMouseHeld = new boolean[2];
    private double lastMouseX;
    private double lastMouseY;
    private final String currentName = "macro-1";

    void startRecording(Minecraft client) {
        if (client.player == null || mode != Mode.IDLE) return;
        releasePlaybackKeys(client);
        releasePlaybackMouse(client);
        events.clear();
        tick = 0;
        previous = readMovementKeys(client);
        long window = client.getWindow().handle();
        previousMouse[0] = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        previousMouse[1] = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        double[] pos = cursorPosition(window);
        lastMouseX = pos[0];
        lastMouseY = pos[1];
        mode = Mode.RECORDING;
        message(client, "Rozpoczęto nagrywanie. F7 kończy makro.");
    }

    void stopRecording(Minecraft client) {
        if (mode != Mode.RECORDING) return;
        events.add(new Event(Math.max(1, tick), Action.LOOP_END, false, ""));
        mode = Mode.IDLE;
        save();
        message(client, "Zapisano makro: " + (events.size() - 1) + " zdarzeń.");
    }

    void startPlayback(Minecraft client) {
        if (client.player == null || mode != Mode.IDLE) return;
        load();
        if (events.isEmpty()) {
            message(client, "Brak nagrania. Najpierw nagraj makro klawiszem F6.");
            return;
        }
        events.sort(Comparator.comparingInt(Event::tick));
        playbackIndex = 0;
        tick = 0;
        for (int i = 0; i < playbackHeld.length; i++) playbackHeld[i] = false;
        for (int i = 0; i < playbackMouseHeld.length; i++) playbackMouseHeld[i] = false;
        mode = Mode.PLAYING;
        message(client, "Odtwarzanie makra. F9 zatrzymuje.");
    }

    void stopPlayback(Minecraft client) {
        if (mode != Mode.PLAYING) return;
        mode = Mode.IDLE;
        releasePlaybackKeys(client);
        releasePlaybackMouse(client);
        message(client, "Zatrzymano odtwarzanie.");
    }

    void tick(Minecraft client) {
        if (client.player == null) {
            if (mode == Mode.PLAYING) {
                mode = Mode.IDLE;
                releasePlaybackKeys(client);
                releasePlaybackMouse(client);
            }
            return;
        }
        if (mode == Mode.RECORDING) {
            recordMovement(client);
            recordMouse(client);
            tick++;
        } else if (mode == Mode.PLAYING) {
            playTick(client);
        }
    }

    private void recordMovement(Minecraft client) {
        boolean[] now = readMovementKeys(client);
        Action[] actions = {Action.FORWARD, Action.BACK, Action.LEFT, Action.RIGHT,
                Action.JUMP, Action.SNEAK, Action.SPRINT};
        for (int i = 0; i < now.length; i++) {
            if (now[i] != previous[i]) events.add(new Event(tick, actions[i], now[i], ""));
        }
        previous = now;
    }

    private void recordMouse(Minecraft client) {
        long window = client.getWindow().handle();
        double[] pos = cursorPosition(window);
        if (Math.abs(pos[0] - lastMouseX) >= 1.0 || Math.abs(pos[1] - lastMouseY) >= 1.0) {
            events.add(new Event(tick, Action.MOUSE_MOVE, false,
                    Double.toString(pos[0]) + "," + Double.toString(pos[1])));
            lastMouseX = pos[0];
            lastMouseY = pos[1];
        }

        int[] buttons = {GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_RIGHT};
        Action[] actions = {Action.MOUSE_LEFT, Action.MOUSE_RIGHT};
        for (int i = 0; i < buttons.length; i++) {
            boolean down = GLFW.glfwGetMouseButton(window, buttons[i]) == GLFW.GLFW_PRESS;
            if (down != previousMouse[i]) {
                events.add(new Event(tick, actions[i], down, guiCursorPosition(client, pos)));
                previousMouse[i] = down;
            }
        }
    }

    private double[] cursorPosition(long window) {
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        return new double[]{x[0], y[0]};
    }

    private void playTick(Minecraft client) {
        while (playbackIndex < events.size() && events.get(playbackIndex).tick() <= tick) {
            Event event = events.get(playbackIndex++);
            switch (event.action()) {
                case COMMAND -> client.player.connection.sendCommand(event.value());
                case MOUSE_MOVE -> {
                    String[] coordinates = event.value().split(",", 2);
                    if (coordinates.length == 2) {
                        try {
                            GLFW.glfwSetCursorPos(client.getWindow().handle(),
                                    Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                case MOUSE_LEFT -> setMouseButton(client, 0, event.pressed(), event.value());
                case MOUSE_RIGHT -> setMouseButton(client, 1, event.pressed(), event.value());
                case LOOP_END -> {
                    playbackIndex = 0;
                    tick = 0;
                }
                default -> setMovementKey(client, event.action(), event.pressed());
            }
        }
        tick++;
        if (playbackIndex >= events.size()) {
            mode = Mode.IDLE;
            releasePlaybackKeys(client);
            releasePlaybackMouse(client);
            message(client, "Makro zakończone.");
        }
    }

    private String guiCursorPosition(Minecraft client, double[] pos) {
        double x = pos[0] * client.getWindow().getGuiScaledWidth() / Math.max(1, client.getWindow().getWidth());
        double y = pos[1] * client.getWindow().getGuiScaledHeight() / Math.max(1, client.getWindow().getHeight());
        return Double.toString(x) + "," + Double.toString(y);
    }

    private void setMouseButton(Minecraft client, int index, boolean pressed, String position) {
        if (client.options == null || client.player == null) return;
        String[] coordinates = position.split(",", 2);
        double mouseX = 0.0;
        double mouseY = 0.0;
        if (coordinates.length == 2) {
            try {
                mouseX = Double.parseDouble(coordinates[0]);
                mouseY = Double.parseDouble(coordinates[1]);
            } catch (NumberFormatException ignored) {}
        }
        int button = index == 0 ? GLFW.GLFW_MOUSE_BUTTON_LEFT : GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        if (client.screen != null) {
            MouseButtonEvent mouseEvent = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
            if (pressed) client.screen.mouseClicked(mouseEvent, false);
            else client.screen.mouseReleased(mouseEvent);
            playbackMouseHeld[index] = pressed;
            return;
        }
        if (client.gameMode == null) return;
        KeyMapping key = index == 0 ? client.options.keyAttack : client.options.keyUse;
        if (pressed) {
            if (index == 0) {
                // Perform an actual attack or begin breaking the targeted block.
                if (client.hitResult instanceof EntityHitResult entityHit) {
                    client.gameMode.attack(client.player, entityHit.getEntity());
                } else if (client.hitResult instanceof BlockHitResult blockHit) {
                    client.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
                } else {
                    client.player.swing(InteractionHand.MAIN_HAND);
                }
            } else {
                // Perform the normal right-click interaction for the current target.
                if (client.hitResult instanceof EntityHitResult entityHit) {
                    client.gameMode.interact(client.player, entityHit.getEntity(), InteractionHand.MAIN_HAND);
                } else if (client.hitResult instanceof BlockHitResult blockHit) {
                    client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, blockHit);
                } else {
                    client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                }
            }
        } else if (index == 0) {
            client.gameMode.stopDestroyBlock();
        }
        key.setDown(pressed);
        playbackMouseHeld[index] = pressed;
    }

    private boolean[] readMovementKeys(Minecraft client) {
        return new boolean[]{client.options.keyUp.isDown(), client.options.keyDown.isDown(),
                client.options.keyLeft.isDown(), client.options.keyRight.isDown(),
                client.options.keyJump.isDown(), client.options.keyShift.isDown(),
                client.options.keySprint.isDown()};
    }

    private void setMovementKey(Minecraft client, Action action, boolean pressed) {
        int index = switch (action) {
            case FORWARD -> 0; case BACK -> 1; case LEFT -> 2; case RIGHT -> 3;
            case JUMP -> 4; case SNEAK -> 5; case SPRINT -> 6; default -> -1;
        };
        if (index < 0) return;
        KeyMapping key = switch (index) {
            case 0 -> client.options.keyUp; case 1 -> client.options.keyDown;
            case 2 -> client.options.keyLeft; case 3 -> client.options.keyRight;
            case 4 -> client.options.keyJump; case 5 -> client.options.keyShift;
            default -> client.options.keySprint;
        };
        key.setDown(pressed);
        playbackHeld[index] = pressed;
    }

    private void releasePlaybackKeys(Minecraft client) {
        if (client == null || client.options == null) return;
        KeyMapping[] keys = {client.options.keyUp, client.options.keyDown, client.options.keyLeft,
                client.options.keyRight, client.options.keyJump, client.options.keyShift,
                client.options.keySprint};
        for (int i = 0; i < keys.length; i++) {
            if (playbackHeld[i]) keys[i].setDown(false);
            playbackHeld[i] = false;
        }
    }

    private void releasePlaybackMouse(Minecraft client) {
        if (client == null || client.getWindow() == null) return;
        if (playbackMouseHeld[0]) setMouseButton(client, 0, false, "0,0");
        if (playbackMouseHeld[1]) setMouseButton(client, 1, false, "0,0");
    }

    private void save() {
        try {
            Path dir = Path.of("config", "macro-recorder");
            Files.createDirectories(dir);
            List<String> lines = new ArrayList<>();
            for (Event e : events) {
                String value = Base64.getEncoder().encodeToString(e.value().getBytes(StandardCharsets.UTF_8));
                lines.add(e.tick() + "|" + e.action().name() + "|" + e.pressed() + "|" + value);
            }
            Files.write(dir.resolve(currentName + ".mcr"), lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    private void load() {
        events.clear();
        Path file = Path.of("config", "macro-recorder", currentName + ".mcr");
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", 4);
                if (p.length != 4) continue;
                String value = new String(Base64.getDecoder().decode(p[3]), StandardCharsets.UTF_8);
                events.add(new Event(Integer.parseInt(p[0]), Action.valueOf(p[1]),
                        Boolean.parseBoolean(p[2]), value));
            }
        } catch (Exception ignored) { events.clear(); }
    }

    void recordCommand(String command) {
        if (mode == Mode.RECORDING) events.add(new Event(tick, Action.COMMAND, true, command));
    }

    boolean shouldShowHud() { return mode != Mode.IDLE; }
    boolean isRecording() { return mode == Mode.RECORDING; }
    String statusText() { return mode == Mode.RECORDING ? "● NAGRYWANIE" : "▶ ODTWARZANIE"; }

    private void message(Minecraft client, String text) {
        if (client.player != null) client.player.displayClientMessage(Component.literal("[Macro] " + text), false);
    }
}
