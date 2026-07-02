package io.github.andrewwwwwwwwwwwwwww.roughlife.item;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLPlayerData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;

/** Clean, filtered water. Fully safe to drink and very hydrating. */
public class PurifiedWaterItem extends Item {
    public PurifiedWaterItem(Properties properties) {
        super(properties.component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player && player instanceof RLPlayerData data) {
            data.roughlife$addThirst(10);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
