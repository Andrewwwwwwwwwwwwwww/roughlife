package io.github.andrewwwwwwwwwwwwwww.roughlife;

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

    public static void init() {
        FabricDefaultAttributeRegistry.register(WAILING_SKULL, WailingSkull.createAttributes());
    }
}
