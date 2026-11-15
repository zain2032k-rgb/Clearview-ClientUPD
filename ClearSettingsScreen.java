package com.clearview.gui;

import com.clearview.ClearViewClient;
import com.clearview.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class ClearSettingsScreen extends Screen {
    private static final int[] STRENGTH = {50, 75, 100, 125, 150};
    private static final int[] SCALE = {40, 50, 60, 70, 80, 90, 100};
    private static final int[] OFFSET = {-30, -20, -10, 0, 10, 20, 30};

    private final Screen parent;

    public ClearSettingsScreen(Screen parent) {
        super(Text.literal("ClearView Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int colW = 150, gap = 6;
        int left = width / 2 - colW - gap / 2;
        int right = width / 2 + gap / 2;
        int y = 40, step = 24;

        row(left, y, colW, () -> "Swing: " + Config.swingStyle.label, () -> Config.swingStyle = Config.swingStyle.next());
        row(right, y, colW, () -> "Swing size: " + Config.strength + "%", () -> Config.strength = cycle(Config.strength, STRENGTH));
        y += step;
        row(left, y, colW, () -> "Item size: " + Config.itemScale + "%", () -> Config.itemScale = cycle(Config.itemScale, SCALE));
        row(right, y, colW, () -> "No equip dip: " + (Config.noEquipDip ? "ON" : "OFF"), () -> Config.noEquipDip = !Config.noEquipDip);
        y += step;
        row(left, y, colW, () -> "Item X: " + Config.offsetX, () -> Config.offsetX = cycle(Config.offsetX, OFFSET));
        row(right, y, colW, () -> "Item Y: " + Config.offsetY, () -> Config.offsetY = cycle(Config.offsetY, OFFSET));
        y += step;
        row(left, y, colW, () -> "Item Z: " + Config.offsetZ, () -> Config.offsetZ = cycle(Config.offsetZ, OFFSET));
        row(right, y, colW, () -> "Main menu: " + Config.menuStyle.label, () -> Config.menuStyle = Config.menuStyle.next());
        y += step;
        row(left, y, colW, () -> "FPS boost: " + (Config.fpsBoost ? "ON" : "OFF"), () -> ClearViewClient.setBoost(!Config.fpsBoost));
        row(right, y, colW, () -> "FPS counter: " + (Config.showFps ? "ON" : "OFF"), () -> Config.showFps = !Config.showFps);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 100, height - 30, 200, 20).build());
    }

    private void row(int x, int y, int w, Supplier<String> label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label.get()), b -> {
            action.run();
            b.setMessage(Text.literal(label.get()));
            Config.save();
        }).dimensions(x, y, w, 20).build());
    }

    private static int cycle(int value, int[] options) {
        for (int o : options) if (o > value) return o;
        return options[0];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Totems: Options > Resource Packs > ClearView Totem ..."),
                width / 2, 40 + 24 * 4 + 8, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
