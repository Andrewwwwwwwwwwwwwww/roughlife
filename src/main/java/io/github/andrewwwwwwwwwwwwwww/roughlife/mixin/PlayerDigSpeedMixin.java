package io.github.andrewwwwwwwwwwwwwww.roughlife.mixin;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLConfig;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Punching trees barehanded is miserable: logs break extremely slowly
 * without an axe. Knap a flint hatchet first.
 */
@Mixin(Player.class)
public abstract class PlayerDigSpeedMixin {
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void roughlife$slowLogPunching(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (!RLConfig.get().slowTreePunching) {
            return;
        }
        Player self = (Player) (Object) this;
        if (state.is(BlockTags.LOGS) && !self.getMainHandItem().is(ItemTags.AXES)) {
            cir.setReturnValue(cir.getReturnValue() * 0.12f);
        }
    }
}
