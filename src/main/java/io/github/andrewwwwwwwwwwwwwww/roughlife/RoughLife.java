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
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            // Plant fiber from grass — the early-game string substitute.
            if (level instanceof ServerLevel serverLevel
                    && (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)
                        || state.is(Blocks.LARGE_FERN))
                    && serverLevel.getRandom().nextFloat() < 0.30f) {
                Block.popResource(serverLevel, pos, new ItemStack(RLItems.PLANT_FIBER));
            }
        });
        UseBlockCallback.EVENT.register(this::onKnapping);

        LOGGER.info("Rough Life initialized — stay hydrated, stay warm, don't punch trees.");
    }

    /** Right-clicking stone with flint knaps it into a flint shard (with a chance of ruining it). */
    private InteractionResult onKnapping(net.minecraft.world.entity.player.Player player,
                                         net.minecraft.world.level.Level level,
                                         net.minecraft.world.InteractionHand hand,
                                         net.minecraft.world.phys.BlockHitResult hit) {
        if (!RLConfig.get().knappingEnabled) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.FLINT)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        if (!level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)
                && !level.getBlockState(pos).is(Blocks.COBBLESTONE)
                && !level.getBlockState(pos).is(Blocks.STONE_BRICKS)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            held.shrink(1);
            if (serverLevel.getRandom().nextFloat() < 0.60f) {
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
