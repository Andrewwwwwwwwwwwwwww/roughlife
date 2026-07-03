package io.github.andrewwwwwwwwwwwwwww.roughlife;

import io.github.andrewwwwwwwwwwwwwww.roughlife.entity.Razorwing;
import io.github.andrewwwwwwwwwwwwwww.roughlife.entity.StingerJelly;
import io.github.andrewwwwwwwwwwwwwww.roughlife.entity.WailingSkull;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class RLEntities {
    private RLEntities() {}

    public static final ResourceKey<EntityType<?>> WAILING_SKULL_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "wailing_skull"));

    public static final EntityType<WailingSkull> WAILING_SKULL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, WAILING_SKULL_KEY,
            EntityType.Builder.of(WailingSkull::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.6f)
                    .eyeHeight(0.3f)
                    .clientTrackingRange(10)
                    .build(WAILING_SKULL_KEY));

    public static final ResourceKey<EntityType<?>> RAZORWING_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "razorwing"));

    public static final EntityType<Razorwing> RAZORWING = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, RAZORWING_KEY,
            EntityType.Builder.of(Razorwing::new, MobCategory.MONSTER)
                    .sized(0.9f, 0.5f)
                    .eyeHeight(0.25f)
                    .clientTrackingRange(10)
                    .build(RAZORWING_KEY));

    public static final ResourceKey<EntityType<?>> STINGER_JELLY_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "stinger_jelly"));

    public static final EntityType<StingerJelly> STINGER_JELLY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, STINGER_JELLY_KEY,
            EntityType.Builder.of(StingerJelly::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.9f)
                    .eyeHeight(0.5f)
                    .clientTrackingRange(10)
                    .build(STINGER_JELLY_KEY));

    public static void init() {
        FabricDefaultAttributeRegistry.register(WAILING_SKULL, WailingSkull.createAttributes());
        FabricDefaultAttributeRegistry.register(RAZORWING, Razorwing.createAttributes());
        FabricDefaultAttributeRegistry.register(STINGER_JELLY, StingerJelly.createAttributes());
    }
}
