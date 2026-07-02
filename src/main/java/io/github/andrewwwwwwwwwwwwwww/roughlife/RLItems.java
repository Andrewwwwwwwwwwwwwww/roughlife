package io.github.andrewwwwwwwwwwwwwww.roughlife;

import io.github.andrewwwwwwwwwwwwwww.roughlife.item.BandageItem;
import io.github.andrewwwwwwwwwwwwwww.roughlife.item.CanteenItem;
import io.github.andrewwwwwwwwwwwwwww.roughlife.item.PurifiedWaterItem;
import io.github.andrewwwwwwwwwwwwwww.roughlife.item.SplintItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;

import java.util.function.Function;

public final class RLItems {
    private RLItems() {}

    /** Weak knapped-flint tool tier, slightly below wood in damage but faster than bare hands. */
    public static final ToolMaterial FLINT_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 90, 3.0f, 1.0f, 5,
            TagKey.create(Registries.ITEM, id("repairs_flint_tools")));

    public static final Item FLINT_SHARD = register("flint_shard", Item::new,
            new Item.Properties());

    public static final Item PLANT_FIBER = register("plant_fiber", Item::new,
            new Item.Properties());

    public static final Item FLINT_KNIFE = register("flint_knife", Item::new,
            new Item.Properties().sword(FLINT_MATERIAL, 2.0f, -2.0f).stacksTo(1));

    public static final Item FLINT_HATCHET = register("flint_hatchet", Item::new,
            new Item.Properties().axe(FLINT_MATERIAL, 4.0f, -3.2f).stacksTo(1));

    public static final Item BANDAGE = register("bandage", BandageItem::new,
            new Item.Properties().stacksTo(16));

    public static final Item SPLINT = register("splint", SplintItem::new,
            new Item.Properties().stacksTo(16));

    public static final Item CANTEEN = register("canteen", CanteenItem::new,
            new Item.Properties().stacksTo(1).durability(CanteenItem.SIPS));

    public static final Item CHARCOAL_FILTER = register("charcoal_filter", Item::new,
            new Item.Properties().stacksTo(16));

    public static final Item PURIFIED_WATER_BOTTLE = register("purified_water_bottle", PurifiedWaterItem::new,
            new Item.Properties().stacksTo(1).usingConvertsTo(Items.GLASS_BOTTLE));

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, path);
    }

    private static <I extends Item> I register(String name, Function<Item.Properties, I> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
        I item = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(FLINT_KNIFE);
            output.accept(FLINT_HATCHET);
            output.accept(CANTEEN);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(FLINT_SHARD);
            output.accept(PLANT_FIBER);
            output.accept(CHARCOAL_FILTER);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
            output.accept(PURIFIED_WATER_BOTTLE);
            output.accept(BANDAGE);
            output.accept(SPLINT);
        });
    }
}
