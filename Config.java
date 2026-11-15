package com.clearview;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    public static SwingStyle swingStyle = SwingStyle.SMOOTH;
    public static int strength = 100;      // swing size, percent
    public static int itemScale = 70;      // held item size, percent
    public static int offsetX = 0;         // hundredths of a block
    public static int offsetY = 0;
    public static int offsetZ = 0;
    public static boolean noEquipDip = true;
    public static MenuStyle menuStyle = MenuStyle.AURORA;
    public static boolean fpsBoost = true;
    public static boolean showFps = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("clearview.json");
    }

    private static class Data {
        SwingStyle swingStyle = Config.swingStyle;
        int strength = Config.strength;
        int itemScale = Config.itemScale;
        int offsetX = Config.offsetX;
        int offsetY = Config.offsetY;
        int offsetZ = Config.offsetZ;
        boolean noEquipDip = Config.noEquipDip;
        MenuStyle menuStyle = Config.menuStyle;
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
            itemScale = d.itemScale;
            offsetX = d.offsetX;
            offsetY = d.offsetY;
            offsetZ = d.offsetZ;
            noEquipDip = d.noEquipDip;
            if (d.menuStyle != null) menuStyle = d.menuStyle;
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
