package com.clearview;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;

/** Plays a keyboard sound on every key press by chaining onto the game's own key callback. */
public final class CreamyKeys {
    private static long lastNs;
    private static boolean installed;

    public static void install(MinecraftClient client) {
        if (installed) return;
        installed = true;
        long handle = client.getWindow().getHandle();
        GLFWKeyCallback[] prev = new GLFWKeyCallback[1];
        GLFWKeyCallbackI mine = (window, key, scancode, action, mods) -> {
            try {
                onKey(key, action);
            } catch (Throwable ignored) {
            }
            if (prev[0] != null) prev[0].invoke(window, key, scancode, action, mods);
        };
        prev[0] = GLFW.glfwSetKeyCallback(handle, mine);
        if (Config.keysOn) KeySynth.init();
    }

    private static boolean isMovement(int key) {
        return key == GLFW.GLFW_KEY_W || key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_S
                || key == GLFW.GLFW_KEY_D || key == GLFW.GLFW_KEY_SPACE
                || key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_LEFT_CONTROL;
    }

    private static void onKey(int key, int action) {
        if (!Config.keysOn || key < 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean inScreen = client.currentScreen != null;
        if (!inScreen && !Config.keyInGame) return;
        if (!inScreen && !Config.keyMovement && isMovement(key)) return;
        if (action == GLFW.GLFW_REPEAT && !inScreen) return;

        boolean release = action == GLFW.GLFW_RELEASE;
        if (release && !Config.keyRelease) return;
        if (!release) {
            long now = System.nanoTime();
            if (now - lastNs < Config.keyCooldown * 1_000_000L) return;
            lastNs = now;
        }
        KeySynth.play(Config.keyProfile, release, key == GLFW.GLFW_KEY_SPACE, key, Config.keyVolume);
    }

    private CreamyKeys() {}
}
