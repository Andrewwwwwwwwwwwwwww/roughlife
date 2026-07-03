package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

import java.util.Set;

/**
 * Death is a journey: without a bed or respawn anchor you never wake up in
 * the same place twice — each death scatters you to a fresh random spot in
 * the wilderness ("always" mode scatters even bed owners).
 */
public final class RespawnScatter {
    private RespawnScatter() {}

    /** Players whose scatter runs next tick, after every respawn handler. */
    private static final java.util.List<ServerPlayer> PENDING = new java.util.ArrayList<>();

    public static void init() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) {
                return; // end-portal return, not a death
            }
            String mode = RLConfig.get().scatterRespawns;
            if (mode.equals("off")) {
                return;
            }
            if (mode.equals("no-bed") && newPlayer.getRespawnConfig() != null) {
                return; // they earned a fixed spawn
            }
            // Defer to the next server tick so no other respawn handler
            // (e.g., an exact-spawn mod running after us in the same event)
            // can teleport the player back and silently undo the scatter.
            PENDING.add(newPlayer);
        });
    }

    /** Called once per server tick from RoughLife. */
    public static void flush() {
        if (PENDING.isEmpty()) {
            return;
        }
        for (ServerPlayer player : PENDING) {
            if (!player.isRemoved()) {
                scatter(player);
            }
        }
        PENDING.clear();
    }

    private static void scatter(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        RandomSource random = level.getRandom();
        int radius = Math.max(200, RLConfig.get().scatterRadius);
        BlockPos center = level.getRespawnData().pos();
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = center.getX() + (int) ((random.nextDouble() * 2 - 1) * radius);
            int z = center.getZ() + (int) ((random.nextDouble() * 2 - 1) * radius);
            if (!level.getWorldBorder().isWithinBounds(x, z)) {
                continue;
            }
            level.getChunk(x >> 4, z >> 4); // force-generate the column
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockState ground = level.getBlockState(new BlockPos(x, y - 1, z));
            if (ground.isAir() || !ground.getFluidState().isEmpty()) {
                continue; // void floor or water/lava — keep looking
            }
            player.teleportTo(level, x + 0.5, y, z + 0.5, Set.of(),
                    player.getYRot(), player.getXRot(), true);
            player.sendSystemMessage(Component.literal(
                    "You wake somewhere unfamiliar... (" + x + ", " + y + ", " + z + ")"));
            RoughLife.LOGGER.info("Respawn scatter: {} -> {}, {}, {}", player.getName().getString(), x, y, z);
            return;
        }
        RoughLife.LOGGER.warn("Respawn scatter: no safe spot found for {} after 24 attempts", player.getName().getString());
    }
}
