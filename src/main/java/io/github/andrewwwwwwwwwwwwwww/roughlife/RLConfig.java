package io.github.andrewwwwwwwwwwwwwww.roughlife;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Global mod config, stored at config/roughlife.json.
 * Loaded once at startup; /roughlife reload re-reads it.
 */
public final class RLConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static RLConfig instance = new RLConfig();

    public boolean thirstEnabled = true;
    public boolean temperatureEnabled = true;
    public boolean bleedingEnabled = true;
    public boolean fracturesEnabled = true;
    public boolean slowTreePunching = true;
    public boolean knappingEnabled = true;
    public boolean dirtyWaterSickness = true;
    /** "vanilla" = untouched, "slow" = slow food-gated regen, "off" = items only. */
    public String naturalRegen = "slow";
    public boolean thirstDamageKills = false;
    public double thirstDrainMultiplier = 1.0;

    public static RLConfig get() {
        return instance;
    }

    public static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("roughlife.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                RLConfig loaded = GSON.fromJson(Files.readString(p), RLConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            }
            save();
        } catch (Exception e) {
            RoughLife.LOGGER.error("Failed to load roughlife.json, using defaults", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(instance));
        } catch (Exception e) {
            RoughLife.LOGGER.error("Failed to save roughlife.json", e);
        }
    }
}
