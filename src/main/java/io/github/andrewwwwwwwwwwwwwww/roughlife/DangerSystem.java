package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

/**
 * "Dangerous World": keeps hostile pressure on players at night and
 * underground by topping up natural spawns near them, so exploring in the
 * dark is a constant threat instead of an occasional one. Respects a nearby
 * hostile cap and only places mobs on valid dark ground out of melee range.
 */
public final class DangerSystem {
    private DangerSystem() {}

    public static void tick(ServerPlayer player, boolean bloodMoon) {
        ServerLevel level = player.level();
        if (level.getDifficulty() == Difficulty.PEACEFUL || !level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        boolean night = level.getSkyDarken() >= 8;
        boolean underground = level.getBrightness(LightLayer.SKY, player.blockPosition()) == 0;
        if (!night && !underground) {
            return;
        }
        int cap = Math.max(1, RLConfig.get().dangerMaxNearbyHostiles);
        if (bloodMoon) {
            cap += cap / 2;
        }
        AABB box = new AABB(player.blockPosition()).inflate(48.0, 32.0, 48.0);
        if (level.getEntitiesOfClass(Monster.class, box).size() >= cap) {
            return;
        }

        RandomSource random = player.getRandom();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 20.0 + random.nextDouble() * 22.0;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
            BlockPos spot = findSpot(level, x, player.getBlockY(), z);
            if (spot == null) {
                continue;
            }
            // Only in the dark: don't undermine torch-lit bases.
            if (level.getBrightness(LightLayer.BLOCK, spot) > 0) {
                continue;
            }
            if (!night && level.getBrightness(LightLayer.SKY, spot) > 0) {
                continue;
            }
            spawnWeighted(level, spot, random, bloodMoon);
            return;
        }
    }

    /** Finds standable ground (solid below, two air blocks) near the player's y. */
    private static BlockPos findSpot(ServerLevel level, int x, int playerY, int z) {
        for (int dy = 6; dy >= -6; dy--) {
            BlockPos pos = new BlockPos(x, playerY + dy, z);
            if (level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()
                    && !level.getBlockState(pos.below()).isAir()
                    && level.getBlockState(pos.below()).isSolid()) {
                return pos;
            }
        }
        return null;
    }

    private static void spawnWeighted(ServerLevel level, BlockPos pos, RandomSource random, boolean bloodMoon) {
        int roll = random.nextInt(10);
        EntityType<? extends Monster> type;
        if (roll < 4) {
            type = EntityTypes.ZOMBIE;
        } else if (roll < 7) {
            type = EntityTypes.SKELETON;
        } else if (roll < 9) {
            type = EntityTypes.SPIDER;
        } else {
            type = EntityTypes.CREEPER;
        }
        Monster mob = type.spawn(level, pos, EntitySpawnReason.NATURAL);
        if (mob != null && bloodMoon) {
            int tenMinutes = 20 * 60 * 10;
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.SPEED, tenMinutes, 0, true, false, false));
            mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.STRENGTH, tenMinutes, 0, true, false, false));
        }
    }
}
