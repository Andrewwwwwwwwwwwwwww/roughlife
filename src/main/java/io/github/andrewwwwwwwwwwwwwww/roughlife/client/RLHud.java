package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RoughLife;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Draws the thirst bar (10 droplets, above the food bar) and a body
 * temperature indicator to the left of the hotbar.
 */
public class RLHud implements HudElement {
    private static final Identifier DROPLETS =
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "textures/gui/droplets.png");
    private static final Identifier TEMPERATURE =
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "textures/gui/temperature.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Thirst: right-aligned row above the food bar; shift up when the air bar shows.
        int thirst = ClientStats.thirst;
        int y = height - 49;
        if (mc.player.isUnderWater() || mc.player.getAirSupply() < mc.player.getMaxAirSupply()) {
            y -= 10;
        }
        for (int i = 0; i < 10; i++) {
            int x = width / 2 + 91 - i * 8 - 9;
            float u;
            if (thirst >= 2 * i + 2) {
                u = 0.0f; // full droplet
            } else if (thirst == 2 * i + 1) {
                u = 9.0f; // half droplet
            } else {
                u = 18.0f; // empty
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, DROPLETS, x, y, u, 0.0f, 9, 9, 27, 9);
        }

        // Temperature icon: five states from freezing to burning, left of the hotbar.
        int temp = ClientStats.temperature;
        int bracket;
        if (temp < 4) {
            bracket = 0;
        } else if (temp < 8) {
            bracket = 1;
        } else if (temp <= 32) {
            bracket = 2;
        } else if (temp <= 36) {
            bracket = 3;
        } else {
            bracket = 4;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEMPERATURE,
                width / 2 - 91 - 20, height - 19, bracket * 16.0f, 0.0f, 16, 16, 80, 16);
    }
}
