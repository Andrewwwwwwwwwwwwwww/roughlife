package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Death is a journey: without a bed or respawn anchor you never wake up in
 * the same place twice. The destination hunt starts THE MOMENT you die —
 * the target chunk generates asynchronously while the death screen is up —
 * so clicking Respawn places you at the new spot immediately instead of
 * freezing the server on main-thread chunk generation.
 */
public final class RespawnScatter {
    private RespawnScatter() {}

    private record Target(int chunkX, int chunkZ,
                          CompletableFuture<ChunkResult<ChunkAccess>> future, int attempt) {}

    private static final Map<UUID, Target> TARGETS = new HashMap<>();
    private static final Map<UUID, Integer> WAIT_TICKS = new HashMap<>();
    /** Players who have respawned and are waiting for placement. */
    private static final List<ServerPlayer> PENDING = new ArrayList<>();

    private static final int MAX_CHUNK_ATTEMPTS = 5;
    private static final int MAX_WAIT_TICKS = 200; // 10s safety valve

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player && applies(player)) {
                beginSearch(player.level().getServer().overworld(), player.getUUID(), 0);
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive || !applies(newPlayer)) {
                return;
            }
            PENDING.add(newPlayer);
            WAIT_TICKS.put(newPlayer.getUUID(), 0);
        });
    }

    private static boolean applies(ServerPlayer player) {
        String mode = RLConfig.get().scatterRespawns;
        if (mode.equals("off")) {
            return false;
        }
        return !mode.equals("no-bed") || player.getRespawnConfig() == null;
    }

    /** Picks a random spot and starts generating its chunk off-thread. */
    private static void beginSearch(ServerLevel overworld, UUID id, int attempt) {
        RandomSource random = overworld.getRandom();
        int radius = Math.max(200, RLConfig.get().scatterRadius);
        BlockPos center = overworld.getRespawnData().pos();
        int x = center.getX() + (int) ((random.nextDouble() * 2 - 1) * radius);
        int z = center.getZ() + (int) ((random.nextDouble() * 2 - 1) * radius);
        if (!overworld.getWorldBorder().isWithinBounds(x, z)) {
            x = center.getX() + (int) ((random.nextDouble() * 2 - 1) * 200);
            z = center.getZ() + (int) ((random.nextDouble() * 2 - 1) * 200);
        }
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        TARGETS.put(id, new Target(chunkX, chunkZ,
                overworld.getChunkSource().getChunkFuture(chunkX, chunkZ, ChunkStatus.FULL, true), attempt));
    }

    /** Called once per server tick from RoughLife. */
    public static void flush() {
        if (PENDING.isEmpty()) {
            return;
        }
        Iterator<ServerPlayer> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            UUID id = player.getUUID();
            if (player.isRemoved()) {
                iterator.remove();
                TARGETS.remove(id);
                WAIT_TICKS.remove(id);
                continue;
            }
            ServerLevel overworld = player.level().getServer().overworld();
            Target target = TARGETS.get(id);
            if (target == null) {
                // Death event was missed somehow — start the hunt now.
                beginSearch(overworld, id, 0);
                continue;
            }
            int waited = WAIT_TICKS.merge(id, 1, Integer::sum);
            if (!target.future().isDone()) {
                if (waited > MAX_WAIT_TICKS) {
                    RoughLife.LOGGER.warn("Respawn scatter: chunk generation timed out for {}; leaving them at world spawn",
                            player.getName().getString());
                    iterator.remove();
                    TARGETS.remove(id);
                    WAIT_TICKS.remove(id);
                }
                continue;
            }

            ChunkResult<ChunkAccess> result = target.future().getNow(null);
            ChunkAccess chunk = result != null && result.isSuccess() ? result.orElse(null) : null;
            BlockPos spot = chunk == null ? null
                    : findSafeColumn(overworld, chunk, target.chunkX(), target.chunkZ());
            if (spot != null) {
                player.teleportTo(overworld, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5,
                        Set.of(), player.getYRot(), player.getXRot(), true);
                player.sendSystemMessage(Component.literal("You wake somewhere unfamiliar... ("
                        + spot.getX() + ", " + spot.getY() + ", " + spot.getZ() + ")"));
                RoughLife.LOGGER.info("Respawn scatter: {} -> {}, {}, {} (chunk attempt {})",
                        player.getName().getString(), spot.getX(), spot.getY(), spot.getZ(), target.attempt());
                iterator.remove();
                TARGETS.remove(id);
                WAIT_TICKS.remove(id);
            } else if (target.attempt() < MAX_CHUNK_ATTEMPTS) {
                // Ocean or void chunk — hunt somewhere else, still off-thread.
                beginSearch(overworld, id, target.attempt() + 1);
            } else {
                RoughLife.LOGGER.warn("Respawn scatter: no dry land found for {} after {} chunks; leaving them at world spawn",
                        player.getName().getString(), MAX_CHUNK_ATTEMPTS + 1);
                iterator.remove();
                TARGETS.remove(id);
                WAIT_TICKS.remove(id);
            }
        }
    }

    /**
     * Scans the generated chunk's heightmap for a landing column. Waking up
     * mid-ocean is fine (you surface swimming) — only void and lava columns
     * are rejected.
     */
    private static BlockPos findSafeColumn(ServerLevel level, ChunkAccess chunk, int chunkX, int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int start = level.getRandom().nextInt(256);
        for (int i = 0; i < 256; i++) {
            int index = (start + i) & 255;
            int localX = index & 15;
            int localZ = index >> 4;
            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, localX, localZ);
            BlockPos top = new BlockPos(baseX + localX, y - 1, baseZ + localZ);
            BlockState state = level.getBlockState(top);
            if (state.isAir()) {
                continue; // void column
            }
            if (!state.getFluidState().isEmpty() && !state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                continue; // lava lake — no
            }
            return top.above();
        }
        return null;
    }
}
