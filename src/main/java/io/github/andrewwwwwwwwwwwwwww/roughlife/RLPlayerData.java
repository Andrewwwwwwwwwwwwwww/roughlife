package io.github.andrewwwwwwwwwwwwwww.roughlife;

/**
 * Duck interface implemented onto Player via PlayerDataMixin.
 * Thirst is 0..20 (like food), temperature is degrees on a 0..40 scale
 * where 20 is comfortable.
 */
public interface RLPlayerData {
    int roughlife$getThirst();

    void roughlife$setThirst(int value);

    float roughlife$getThirstExhaustion();

    void roughlife$setThirstExhaustion(float value);

    float roughlife$getTemperature();

    void roughlife$setTemperature(float value);

    default void roughlife$addThirst(int amount) {
        roughlife$setThirst(Math.max(0, Math.min(20, roughlife$getThirst() + amount)));
    }
}
