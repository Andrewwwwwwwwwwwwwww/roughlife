package io.github.andrewwwwwwwwwwwwwww.roughlife.mixin;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Gates vanilla food-based health regeneration. In "slow" and "off" modes the
 * vanilla heal calls inside FoodData.tick are suppressed; "slow" regen is
 * implemented separately in RoughLife's server tick.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Redirect(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V"))
    private void roughlife$gateNaturalRegen(ServerPlayer player, float amount) {
        if (RLConfig.get().naturalRegen.equals("vanilla")) {
            player.heal(amount);
        }
    }
}
