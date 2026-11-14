package pl.bimber.macro;

import org.lwjgl.glfw.GLFW;

/**
 * Supplies recorded keyboard modifiers while a recorded GUI mouse event is replayed.
 * -1 means that normal physical keyboard state should be used.
 */
public final class PlaybackModifiers {
    private static volatile int modifiers = -1;

    private PlaybackModifiers() {}

    public static void set(int value) {
        modifiers = value;
    }

    public static void clear() {
        modifiers = -1;
    }

    public static boolean isActive() {
        return modifiers != -1;
    }

    public static boolean shiftDown() {
        return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
    }

    public static boolean controlDown() {
        return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
    }

    public static boolean altDown() {
        return (modifiers & GLFW.GLFW_MOD_ALT) != 0;
    }
}
