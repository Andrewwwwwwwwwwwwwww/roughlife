package io.github.andrewwwwwwwwwwwwwww.roughlife.item;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLConfig;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLEffects;
import io.github.andrewwwwwwwwwwwwwww.roughlife.RLPlayerData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;

/**
 * Murky scavenged water. Drinkable in a pinch (small thirst restore, high
 * sickness risk) — or cook it in a furnace/campfire/smoker into Purified
 * Water for a big, safe restore.
 */
public class DirtyWaterItem extends Item {
    public DirtyWaterItem(Properties properties) {
        super(properties.component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player && player instanceof RLPlayerData data) {
            data.roughlife$addThirst(3);
            if (RLConfig.get().dirtyWaterSickness && level.getRandom().nextFloat() < 0.60f) {
                player.addEffect(new MobEffectInstance(RLEffects.GRIMY_GUT, 20 * 25, 0));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
