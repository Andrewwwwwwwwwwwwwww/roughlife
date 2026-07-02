package io.github.andrewwwwwwwwwwwwwww.roughlife.mixin;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RLPlayerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Attaches thirst and body-temperature state to players and persists it
 * with the player's NBT. Values reset naturally on death (fresh entity).
 */
@Mixin(Player.class)
public abstract class PlayerDataMixin implements RLPlayerData {
    @Unique
    private boolean roughlife$initialized;
    @Unique
    private int roughlife$thirst;
    @Unique
    private float roughlife$thirstExhaustion;
    @Unique
    private float roughlife$temperature;

    @Unique
    private void roughlife$ensureInit() {
        if (!roughlife$initialized) {
            roughlife$initialized = true;
            roughlife$thirst = 20;
            roughlife$temperature = 20.0f;
        }
    }

    @Override
    public int roughlife$getThirst() {
        roughlife$ensureInit();
        return roughlife$thirst;
    }

    @Override
    public void roughlife$setThirst(int value) {
        roughlife$ensureInit();
        roughlife$thirst = Math.max(0, Math.min(20, value));
    }

    @Override
    public float roughlife$getThirstExhaustion() {
        roughlife$ensureInit();
        return roughlife$thirstExhaustion;
    }

    @Override
    public void roughlife$setThirstExhaustion(float value) {
        roughlife$ensureInit();
        roughlife$thirstExhaustion = value;
    }

    @Override
    public float roughlife$getTemperature() {
        roughlife$ensureInit();
        return roughlife$temperature;
    }

    @Override
    public void roughlife$setTemperature(float value) {
        roughlife$ensureInit();
        roughlife$temperature = Math.max(0.0f, Math.min(40.0f, value));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void roughlife$save(ValueOutput output, CallbackInfo ci) {
        roughlife$ensureInit();
        output.putInt("roughlife_thirst", roughlife$thirst);
        output.putFloat("roughlife_thirst_exhaustion", roughlife$thirstExhaustion);
        output.putFloat("roughlife_temperature", roughlife$temperature);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void roughlife$load(ValueInput input, CallbackInfo ci) {
        roughlife$initialized = true;
        roughlife$thirst = input.getIntOr("roughlife_thirst", 20);
        roughlife$thirstExhaustion = input.getFloatOr("roughlife_thirst_exhaustion", 0.0f);
        roughlife$temperature = input.getFloatOr("roughlife_temperature", 20.0f);
    }
}
