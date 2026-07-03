package io.github.andrewwwwwwwwwwwwwww.roughlife;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class RLSounds {
    private RLSounds() {}

    public static final SoundEvent RAZORWING_BUZZ = register("entity.razorwing.buzz");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
        // Forces static initialization / registration.
    }
}
