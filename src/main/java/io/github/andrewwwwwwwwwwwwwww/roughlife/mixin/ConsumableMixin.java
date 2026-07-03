package io.github.andrewwwwwwwwwwwwwww.roughlife.mixin;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLConfig;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLEffects;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLPlayerData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla water bottles quench thirst too (they're river water — a modest
 * restore with a sickness gamble). Filter or cook water for a safe drink.
 */
@Mixin(Consumable.class)
public abstract class ConsumableMixin {
    @Inject(method = "onConsume", at = @At("HEAD"))
    private void roughlife$waterBottleThirst(Level level, LivingEntity entity, ItemStack stack,
                                             CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!stack.is(Items.POTION) || !(player instanceof RLPlayerData data)) {
            return;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null && contents.is(Potions.WATER)) {
            data.roughlife$addThirst(4);
            if (RLConfig.get().dirtyWaterSickness && level.getRandom().nextFloat() < 0.35f) {
                player.addEffect(new MobEffectInstance(RLEffects.GRIMY_GUT, 20 * 20, 0));
            }
        }
    }
}
