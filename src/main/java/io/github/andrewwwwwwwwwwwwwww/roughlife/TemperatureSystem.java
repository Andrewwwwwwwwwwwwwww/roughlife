package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Per-player body temperature on a 0..40 scale (20 = comfortable).
 * Recomputed once a second from biome, weather, time, water/fire,
 * nearby heat sources and worn armor; the body value eases toward it.
 */
public final class TemperatureSystem {
    private TemperatureSystem() {}

    public static final float FREEZING = 8.0f;
    public static final float SEVERE_COLD = 4.0f;
    public static final float HOT = 32.0f;
    public static final float SEVERE_HEAT = 36.0f;

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        RLPlayerData data = (RLPlayerData) player;
        ServerLevel level = player.level();
        BlockPos pos = player.blockPosition();

        float target = computeTarget(player, level, pos);
        float current = data.roughlife$getTemperature();
        float step = Math.max(0.05f, Math.abs(target - current) * 0.08f);
        if (current < target) {
            current = Math.min(target, current + step);
        } else {
            current = Math.max(target, current - step);
        }
        data.roughlife$setTemperature(current);

        applyEffects(player, level, current);
    }

    private static float computeTarget(ServerPlayer player, ServerLevel level, BlockPos pos) {
        float target;
        if (level.dimension() == net.minecraft.world.level.Level.NETHER) {
            target = 33.0f;
        } else {
            float biomeTemp = level.getBiome(pos).value().getBaseTemperature();
            target = 10.0f + biomeTemp * 12.5f;
            if (level.getSkyDarken() >= 8) { // night (or heavy storm) chill
                target -= 4.0f;
            }
            if (level.isRainingAt(pos)) {
                target -= 3.0f;
            }
        }

        if (player.isInWater()) {
            target -= 8.0f;
        }
        if (player.isOnFire()) {
            target += 12.0f;
        }
        target += nearbyHeat(level, pos);

        // Armor insulates: it pulls a cold target up, but adds warmth on top of a hot one.
        int pieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                pieces++;
            }
        }
        if (target < 20.0f) {
            target = Math.min(20.0f, target + pieces * 2.5f);
        } else {
            target += pieces * 0.75f;
        }
        return target;
    }

    private static float nearbyHeat(ServerLevel level, BlockPos pos) {
        float best = 0.0f;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 2, 3))) {
            BlockState state = level.getBlockState(p);
            float heat = 0.0f;
            if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
                heat = 9.0f;
            } else if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                    || state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)) {
                if (state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT)) {
                    heat = 8.0f;
                }
            } else if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.LANTERN)) {
                heat = 3.0f;
            } else if (state.is(Blocks.MAGMA_BLOCK)) {
                heat = 6.0f;
            }
            if (heat > best) {
                best = heat;
            }
        }
        return best;
    }

    private static void applyEffects(ServerPlayer player, ServerLevel level, float temp) {
        if (temp < FREEZING) {
            int amplifier = temp < SEVERE_COLD ? 1 : 0;
            player.addEffect(new MobEffectInstance(RLEffects.HYPOTHERMIA, 60, amplifier, true, false, true));
            if (temp < SEVERE_COLD && player.tickCount % 100 == 0) {
                player.hurtServer(level, player.damageSources().freeze(), 1.0f);
            }
        } else if (temp > HOT && !player.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE)) {
            int amplifier = temp > SEVERE_HEAT ? 1 : 0;
            player.addEffect(new MobEffectInstance(RLEffects.HEATSTROKE, 60, amplifier, true, false, true));
            if (temp > SEVERE_HEAT && player.tickCount % 100 == 0) {
                player.hurtServer(level, player.damageSources().hotFloor(), 1.0f);
            }
        }
    }
}
