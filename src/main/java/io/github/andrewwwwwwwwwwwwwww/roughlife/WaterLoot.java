package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Seeds water into the world's chests: dirty bottles are a common find
 * (cook them clean), purified water a rare treat. This is the intended
 * early-game water supply before you can craft a canteen.
 */
public final class WaterLoot {
    private WaterLoot() {}

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            String path = key.identifier().getPath();
            if (!path.startsWith("chests/")) {
                return;
            }
            tableBuilder.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0f))
                    .add(LootItem.lootTableItem(RLItems.DIRTY_WATER_BOTTLE).setWeight(6))
                    .add(LootItem.lootTableItem(RLItems.PURIFIED_WATER_BOTTLE).setWeight(1))
                    .add(EmptyLootItem.emptyItem().setWeight(9)));
        });
    }
}
