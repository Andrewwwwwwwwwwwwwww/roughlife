package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoughLife implements ModInitializer {
    public static final String MOD_ID = "roughlife";
    public static final Logger LOGGER = LoggerFactory.getLogger("RoughLife");

    /** Last values synced to each client, to avoid packet spam. */
    private final Map<UUID, Integer> lastSynced = new HashMap<>();

    @Override
    public void onInitialize() {
        RLConfig.load();
        RLEffects.init();
        RLItems.init();
        RLCommands.init();

        PayloadTypeRegistry.clientboundPlay().register(StatsSyncPayload.TYPE, StatsSyncPayload.CODEC);

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(this::onAfterDamage);
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            // Punching logs bare-handed yields NOTHING: the block is destroyed
            // without drops. Get an axe.
            if (RLConfig.get().slowTreePunching && state.is(BlockTags.LOGS)
                    && !player.getMainHandItem().is(net.minecraft.tags.ItemTags.AXES)
                    && !player.isCreative()) {
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.removeBlock(pos, false);
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendOverlayMessage(
                                net.minecraft.network.chat.Component.literal("Without an axe you just splinter the wood..."));
                    }
                }
                return false; // cancel the vanilla break: no loot, no XP
            }
            return true;
        });
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }
            // Plant fiber from grass — the early-game string substitute.
            if ((state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)
                        || state.is(Blocks.LARGE_FERN))
                    && serverLevel.getRandom().nextFloat() < 0.30f) {
                Block.popResource(serverLevel, pos, new ItemStack(RLItems.PLANT_FIBER));
            }
            // Sticks from trees: punching leaves knocks loose sticks.
            if (state.is(BlockTags.LEAVES) && serverLevel.getRandom().nextFloat() < 0.35f) {
                Block.popResource(serverLevel, pos, new ItemStack(Items.STICK));
            }
            // Tree felling: chopping a log with an axe brings down every
            // connected log above it (sneak to break just the one block).
            if (RLConfig.get().treeFelling && state.is(BlockTags.LOGS)
                    && player.getMainHandItem().is(net.minecraft.tags.ItemTags.AXES)
                    && !player.isShiftKeyDown()) {
                fellTree(serverLevel, pos, player);
            }
        });
        UseBlockCallback.EVENT.register(this::onKnapping);

        LOGGER.info("Rough Life initialized — stay hydrated, stay warm, don't punch trees.");
    }

    /**
     * Fells every log connected to the broken one (at or above the cut),
     * dropping them all and damaging the axe one durability per log.
     */
    private void fellTree(ServerLevel level, BlockPos origin, net.minecraft.world.entity.player.Player player) {
        ItemStack axe = player.getMainHandItem();
        int max = Math.max(1, RLConfig.get().treeFellingMaxLogs);
        java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();
        java.util.HashSet<BlockPos> seen = new java.util.HashSet<>();
        queue.add(origin);
        seen.add(origin);
        int felled = 0;
        while (!queue.isEmpty() && felled < max) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        BlockPos next = current.offset(dx, dy, dz);
                        // Only fell upward from the cut so a low chop takes the
                        // whole tree but never tunnels into logs placed below.
                        if (next.getY() < origin.getY() || !seen.add(next)) {
                            continue;
                        }
                        if (level.getBlockState(next).is(BlockTags.LOGS)) {
                            queue.add(next);
                            level.destroyBlock(next, true, player);
                            felled++;
                            if (!axe.isEmpty() && axe.isDamageableItem()) {
                                axe.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                                if (axe.isEmpty()) {
                                    return; // axe broke mid-tree
                                }
                            }
                            if (felled >= max) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Early-game ground interactions:
     * - sneak + right-click dirt/stone/gravel/sand with an empty hand picks up a Rock;
     * - right-clicking stone while holding flint (60%) or a rock (40%) knaps a flint shard.
     */
    private InteractionResult onKnapping(net.minecraft.world.entity.player.Player player,
                                         net.minecraft.world.level.Level level,
                                         net.minecraft.world.InteractionHand hand,
                                         net.minecraft.world.phys.BlockHitResult hit) {
        if (!RLConfig.get().knappingEnabled || hand != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        BlockPos pos = hit.getBlockPos();
        var state = level.getBlockState(pos);

        // Rock gathering: scrounge the ground with bare hands.
        if (held.isEmpty() && player.isShiftKeyDown()
                && (state.is(BlockTags.DIRT) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                    || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL))) {
            if (level instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, pos.relative(hit.getDirection()), new ItemStack(RLItems.ROCK));
                serverLevel.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.7f, 1.1f);
            }
            return InteractionResult.SUCCESS;
        }

        // Knapping.
        boolean flint = held.is(Items.FLINT);
        boolean rock = held.is(RLItems.ROCK);
        if (!flint && !rock) {
            return InteractionResult.PASS;
        }
        if (!state.is(BlockTags.BASE_STONE_OVERWORLD)
                && !state.is(Blocks.COBBLESTONE)
                && !state.is(Blocks.STONE_BRICKS)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            held.shrink(1);
            float chance = flint ? 0.60f : 0.40f;
            if (serverLevel.getRandom().nextFloat() < chance) {
                Block.popResource(serverLevel, pos.relative(hit.getDirection()), new ItemStack(RLItems.FLINT_SHARD));
                serverLevel.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
            } else {
                serverLevel.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void onAfterDamage(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source,
                               float baseDamage, float damageTaken, boolean blocked) {
        if (!(entity instanceof ServerPlayer player) || blocked) {
            return;
        }
        RLConfig config = RLConfig.get();
        if (config.bleedingEnabled && damageTaken >= 1.5f
                && source.getEntity() instanceof LivingEntity
                && player.getRandom().nextFloat() < 0.25f) {
            player.addEffect(new MobEffectInstance(RLEffects.BLEEDING, 20 * 24, damageTaken >= 8.0f ? 1 : 0));
        }
        if (config.fracturesEnabled && damageTaken >= 3.0f && source.is(DamageTypeTags.IS_FALL)) {
            int duration = 20 * 20 + (int) (damageTaken * 40);
            player.addEffect(new MobEffectInstance(RLEffects.FRACTURE, duration, damageTaken >= 8.0f ? 1 : 0));
        }
    }

    private void onServerTick(MinecraftServer server) {
        RLConfig config = RLConfig.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }
            RLPlayerData data = (RLPlayerData) player;
            if (config.thirstEnabled) {
                ThirstSystem.tick(player);
            }
            if (config.temperatureEnabled) {
                TemperatureSystem.tick(player);
            }
            if (config.dangerousWorld && (player.tickCount + player.getUUID().hashCode() % 50) % Math.max(20, config.dangerIntervalTicks) == 0) {
                DangerSystem.tick(player);
            }
            if (config.naturalRegen.equals("slow") && player.tickCount % 160 == 0
                    && player.isHurt() && !player.hasEffect(RLEffects.BLEEDING)
                    && player.getFoodData().getFoodLevel() >= 16) {
                player.heal(1.0f);
                player.getFoodData().addExhaustion(3.0f);
            }

            // Sync HUD stats when they change (plus a slow heartbeat).
            int packed = data.roughlife$getThirst() << 8 | (int) data.roughlife$getTemperature();
            Integer last = lastSynced.get(player.getUUID());
            if (last == null || last != packed || player.tickCount % 100 == 0) {
                lastSynced.put(player.getUUID(), packed);
                ServerPlayNetworking.send(player,
                        new StatsSyncPayload(data.roughlife$getThirst(), (int) data.roughlife$getTemperature()));
            }
        }
    }
}
