package io.github.andrewwwwwwwwwwwwwww.roughlife.item;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Stops bleeding and restores a little health. */
public class BandageItem extends Item {
    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        boolean bleeding = player.hasEffect(RLEffects.BLEEDING);
        boolean hurt = player.getHealth() < player.getMaxHealth();
        if (!bleeding && !hurt) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            player.removeEffect(RLEffects.BLEEDING);
            player.heal(2.0f);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.blockPosition(),
                    SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
        return InteractionResult.SUCCESS;
    }
}
