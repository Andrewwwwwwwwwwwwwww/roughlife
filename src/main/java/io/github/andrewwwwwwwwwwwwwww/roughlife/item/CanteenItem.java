package io.github.andrewwwwwwwwwwwwwww.roughlife.item;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLConfig;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLEffects;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLPlayerData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.FluidTags;

/**
 * A refillable leather canteen. Right-click a water source to fill it,
 * right-click air to take a sip (raw water: small chance of Grimy Gut).
 * Durability = remaining sips.
 */
public class CanteenItem extends Item {
    public static final int SIPS = 8;

    public CanteenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Look for a water source in reach first: filling wins over sipping.
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(4.5));
        BlockHitResult hit = level.clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            FluidState fluid = level.getFluidState(hit.getBlockPos());
            if (fluid.is(FluidTags.WATER) && stack.getDamageValue() > 0) {
                if (!level.isClientSide()) {
                    stack.setDamageValue(0);
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return InteractionResult.PASS; // empty
        }

        if (!level.isClientSide() && player instanceof RLPlayerData data) {
            data.roughlife$addThirst(4);
            stack.setDamageValue(stack.getDamageValue() + 1);
            if (RLConfig.get().dirtyWaterSickness && level.getRandom().nextFloat() < 0.20f) {
                player.addEffect(new MobEffectInstance(RLEffects.GRIMY_GUT, 20 * 20, 0));
            }
            level.playSound(null, player.blockPosition(),
                    SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }
}
