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

        // The night sky is not empty: wailing skulls and phantom pairs stalk
        // surface travelers (no insomnia required).
        if (night && !underground && random.nextFloat() < 0.12f) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 18.0 + random.nextDouble() * 12.0;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
            BlockPos sky = new BlockPos(x, player.getBlockY() + 14 + random.nextInt(8), z);
            if (level.getBlockState(sky).isAir() && level.getBlockState(sky.above()).isAir()) {
                if (random.nextFloat() < 0.60f) {
                    io.github.andrewwwwwwwwwwwwwww.roughlife.RLEntities.WAILING_SKULL
                            .spawn(level, sky, EntitySpawnReason.NATURAL);
                    io.github.andrewwwwwwwwwwwwwww.roughlife.RLEntities.WAILING_SKULL
                            .spawn(level, sky.above(2), EntitySpawnReason.NATURAL);
                } else {
                    EntityTypes.PHANTOM.spawn(level, sky, EntitySpawnReason.NATURAL);
                    EntityTypes.PHANTOM.spawn(level, sky.above(2), EntitySpawnReason.NATURAL);
                }
                return;
            }
        }

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

    /**
     * Daytime is not safe either: pillager packs roam the surface and drowned
     * shadow anyone crossing open water — RLCraft's "traversal is the danger".
     */
    public static void tickDaytime(ServerPlayer player) {
        ServerLevel level = player.level();
        if (level.getDifficulty() == Difficulty.PEACEFUL || !level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        if (level.getSkyDarken() >= 8) {
            return; // night pressure handles this
        }
        int cap = Math.max(1, RLConfig.get().dangerMaxNearbyHostiles / 2);
        AABB box = new AABB(player.blockPosition()).inflate(56.0, 32.0, 56.0);
        if (level.getEntitiesOfClass(Monster.class, box).size() >= cap) {
            return;
        }
        RandomSource random = player.getRandom();

        // The daytime sky has its own predator: razorwings on patrol.
        if (random.nextFloat() < 0.12f && level.canSeeSky(player.blockPosition())) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 20.0 + random.nextDouble() * 14.0;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
            BlockPos sky = new BlockPos(x, player.getBlockY() + 12 + random.nextInt(8), z);
            if (level.getBlockState(sky).isAir() && level.getBlockState(sky.above()).isAir()) {
                io.github.andrewwwwwwwwwwwwwww.roughlife.RLEntities.RAZORWING
                        .spawn(level, sky, EntitySpawnReason.NATURAL);
                if (random.nextFloat() < 0.4f) {
                    io.github.andrewwwwwwwwwwwwwww.roughlife.RLEntities.RAZORWING
                            .spawn(level, sky.above(2), EntitySpawnReason.NATURAL);
                }
                return;
            }
        }

        // Open water: drowned rise beneath you.
        if (player.isInWater() || level.getBlockState(player.blockPosition().below(2)).getFluidState().isSource()) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double dist = 10.0 + random.nextDouble() * 14.0;
                int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
                int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
                BlockPos spot = findWater(level, x, player.getBlockY(), z);
                if (spot != null) {
                    // Mostly drowned; sometimes a guardian — the closest thing
                    // vanilla has to a sea monster.
                    if (random.nextFloat() < 0.25f) {
                        EntityTypes.GUARDIAN.spawn(level, spot, EntitySpawnReason.NATURAL);
                    } else {
                        EntityTypes.DROWNED.spawn(level, spot, EntitySpawnReason.NATURAL);
                    }
                    return;
                }
            }
            return;
        }

        // Land: a pillager hunting party (2-3, sometimes led by a vindicator).
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 30.0 + random.nextDouble() * 20.0;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
            BlockPos spot = findSpot(level, x, player.getBlockY(), z);
            if (spot == null || level.getBrightness(LightLayer.SKY, spot) == 0) {
                continue;
            }
            int size = 2 + random.nextInt(2);
            for (int i = 0; i < size; i++) {
                if (i == 0 && random.nextFloat() < 0.25f) {
                    EntityTypes.VINDICATOR.spawn(level, spot, EntitySpawnReason.NATURAL);
                } else {
                    EntityTypes.PILLAGER.spawn(level, spot, EntitySpawnReason.NATURAL);
                }
            }
            return;
        }
    }

    /** Finds a water block with water below it (room for a drowned) near y. */
    private static BlockPos findWater(ServerLevel level, int x, int playerY, int z) {
        for (int dy = 2; dy >= -8; dy--) {
            BlockPos pos = new BlockPos(x, playerY + dy, z);
            if (level.getBlockState(pos).getFluidState().isSource()
                    && level.getBlockState(pos.below()).getFluidState().isSource()) {
                return pos.below();
            }
        }
        return null;
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
