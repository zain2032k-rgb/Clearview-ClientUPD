package com.clearview;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    // swing + items
    public static SwingStyle swingStyle = SwingStyle.SMOOTH;
    public static int strength = 100;      // swing size, percent
    public static int swingSpeed = 60;     // percent, lower = slower swing
    public static int itemScale = 70;      // held item size, percent
    public static int offsetX = 0;         // hundredths of a block
    public static int offsetY = 0;
    public static int offsetZ = 0;
    public static boolean noEquipDip = true;

    // creamy keys
    public static boolean keysOn = true;
    public static KeyProfile keyProfile = KeyProfile.CREAMY;
    public static int keyVolume = 60;
    public static boolean keyRelease = true;
    public static int keyCooldown = 15;    // ms between clicks
    public static boolean keyInGame = true;
    public static boolean keyMovement = false;

    // menu + loading
    public static MenuStyle menuStyle = MenuStyle.BLOSSOM;
    public static Accent accent = Accent.BLOSSOM;
    public static boolean blossomLoading = true;
    public static boolean blossomMultiplayer = true;
    public static int blossomMs = 900;

    // performance
    public static boolean fpsBoost = true;
    public static boolean showFps = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("clearview.json");
    }

    private static class Data {
        SwingStyle swingStyle = Config.swingStyle;
        int strength = Config.strength;
        int swingSpeed = Config.swingSpeed;
        int itemScale = Config.itemScale;
        int offsetX = Config.offsetX;
        int offsetY = Config.offsetY;
        int offsetZ = Config.offsetZ;
        boolean noEquipDip = Config.noEquipDip;
        boolean keysOn = Config.keysOn;
        KeyProfile keyProfile = Config.keyProfile;
        int keyVolume = Config.keyVolume;
        boolean keyRelease = Config.keyRelease;
        int keyCooldown = Config.keyCooldown;
        boolean keyInGame = Config.keyInGame;
        boolean keyMovement = Config.keyMovement;
        MenuStyle menuStyle = Config.menuStyle;
        Accent accent = Config.accent;
        boolean blossomLoading = Config.blossomLoading;
        boolean blossomMultiplayer = Config.blossomMultiplayer;
        int blossomMs = Config.blossomMs;
        boolean fpsBoost = Config.fpsBoost;
        boolean showFps = Config.showFps;
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) { save(); return; }
            Data d = GSON.fromJson(Files.readString(f), Data.class);
            if (d == null) return;
            if (d.swingStyle != null) swingStyle = d.swingStyle;
            strength = d.strength;
            swingSpeed = d.swingSpeed;
            itemScale = d.itemScale;
            offsetX = d.offsetX;
            offsetY = d.offsetY;
            offsetZ = d.offsetZ;
            noEquipDip = d.noEquipDip;
            keysOn = d.keysOn;
            if (d.keyProfile != null) keyProfile = d.keyProfile;
            keyVolume = d.keyVolume;
            keyRelease = d.keyRelease;
            keyCooldown = d.keyCooldown;
            keyInGame = d.keyInGame;
            keyMovement = d.keyMovement;
            if (d.menuStyle != null) menuStyle = d.menuStyle;
            if (d.accent != null) accent = d.accent;
            blossomLoading = d.blossomLoading;
            blossomMultiplayer = d.blossomMultiplayer;
            blossomMs = d.blossomMs;
            fpsBoost = d.fpsBoost;
            showFps = d.showFps;
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            Files.writeString(file(), GSON.toJson(new Data()));
        } catch (Exception ignored) {
        }
    }

    private Config() {}
}
