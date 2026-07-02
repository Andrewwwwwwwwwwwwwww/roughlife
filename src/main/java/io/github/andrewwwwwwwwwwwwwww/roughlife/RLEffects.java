package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class RLEffects {
    private RLEffects() {}

    /** Damage over time from open wounds; cured with a bandage. */
    public static final class BleedingEffect extends MobEffect {
        BleedingEffect() {
            super(MobEffectCategory.HARMFUL, 0x8B0000);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            entity.hurtServer(level, entity.damageSources().generic(), 1.0f + amplifier);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 50 == 0;
        }
    }

    /** Adds hunger-drain while overheated. */
    public static final class HeatstrokeEffect extends MobEffect {
        HeatstrokeEffect() {
            super(MobEffectCategory.HARMFUL, 0xE25822);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                player.getFoodData().addExhaustion(0.25f * (amplifier + 1));
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 40 == 0;
        }
    }

    /** Stomach sickness from drinking dirty water; drains food and worsens thirst. */
    public static final class GrimyGutEffect extends MobEffect {
        GrimyGutEffect() {
            super(MobEffectCategory.HARMFUL, 0x6B8E23);
        }

        @Override
        public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                player.getFoodData().addExhaustion(0.8f);
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return duration % 60 == 0;
        }
    }

    public static final Holder<MobEffect> BLEEDING = register("bleeding", new BleedingEffect());

    public static final Holder<MobEffect> FRACTURE = register("fracture",
            new MobEffect(MobEffectCategory.HARMFUL, 0xE8E4D8) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            id("effect.fracture_speed"), -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            id("effect.fracture_attack"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final Holder<MobEffect> HYPOTHERMIA = register("hypothermia",
            new MobEffect(MobEffectCategory.HARMFUL, 0x9BD8FF) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            id("effect.hypothermia_speed"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            id("effect.hypothermia_attack"), -0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final Holder<MobEffect> HEATSTROKE = register("heatstroke", new HeatstrokeEffect());

    public static final Holder<MobEffect> GRIMY_GUT = register("grimy_gut", new GrimyGutEffect());

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, path);
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id(name), effect);
    }

    public static void init() {
        // Forces static initialization / registration.
    }
}
