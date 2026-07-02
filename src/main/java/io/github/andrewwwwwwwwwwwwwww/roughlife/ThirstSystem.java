package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Per-player thirst. Drains with activity, punishes at low levels.
 * Thirst 0..20; exhaustion accumulates and eats a thirst point at 4.0.
 */
public final class ThirstSystem {
    private ThirstSystem() {}

    public static void tick(ServerPlayer player) {
        RLPlayerData data = (RLPlayerData) player;

        float drain = player.isSprinting() ? 0.010f : 0.0016f;
        if (player.hasEffect(RLEffects.GRIMY_GUT)) {
            drain += 0.004f;
        }
        if (data.roughlife$getTemperature() > 30.0f) {
            drain += 0.003f;
        }
        drain *= (float) RLConfig.get().thirstDrainMultiplier;

        float ex = data.roughlife$getThirstExhaustion() + drain;
        if (ex >= 4.0f) {
            ex -= 4.0f;
            data.roughlife$setThirst(Math.max(0, data.roughlife$getThirst() - 1));
        }
        data.roughlife$setThirstExhaustion(ex);

        int thirst = data.roughlife$getThirst();
        if (thirst <= 6 && player.tickCount % 40 == 0) {
            int amplifier = thirst <= 3 ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, amplifier, true, false, true));
            if (thirst <= 3) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false, true));
            }
        }
        if (thirst == 0 && player.tickCount % 80 == 0) {
            boolean canDamage = RLConfig.get().thirstDamageKills || player.getHealth() > 2.0f;
            if (canDamage) {
                player.hurtServer(player.level(), player.damageSources().starve(), 1.0f);
            }
        }
    }
}
