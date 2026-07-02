package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RoughLife;
import io.github.andrewwwwwwwwwwwwwww.roughlife.StatsSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class RoughLifeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StatsSyncPayload.TYPE, (payload, context) -> {
            ClientStats.thirst = payload.thirst();
            ClientStats.temperature = payload.temperature();
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR,
                Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "survival_stats"), new RLHud());
    }
}
