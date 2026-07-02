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

/** Sets a broken bone, removing the fracture effect. */
public class SplintItem extends Item {
    public SplintItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.hasEffect(RLEffects.FRACTURE)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            player.removeEffect(RLEffects.FRACTURE);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, player.blockPosition(),
                    SoundEvents.BAMBOO_PLACE, SoundSource.PLAYERS, 1.0f, 0.9f);
        }
        return InteractionResult.SUCCESS;
    }
}
