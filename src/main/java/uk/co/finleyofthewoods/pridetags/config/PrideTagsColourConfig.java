package uk.co.finleyofthewoods.pridetags.config;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.pridetags.Pridetags;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PrideTagsColourConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrideTagsColourConfig.class);
    private static PrideTagsColourConfig INSTANCE;
    private static final Gson GSON = new Gson();
    private static final File COLOURS_CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pride_tags/teams.json").toFile();

    private final PrideTagsColour[] colours;

    public PrideTagsColourConfig() {
        LOGGER.info("[{}] Loading default config", Pridetags.MOD_ID);
        this.colours = new PrideTagsColour[]{
                new PrideTagsColour("Red", "red"),
                new PrideTagsColour("DarkRed", "dark_red"),
                new PrideTagsColour("Aqua", "aqua"),
                new PrideTagsColour("DarkAqua", "dark_aqua"),
                new PrideTagsColour("Blue", "blue"),
                new PrideTagsColour("DarkBlue", "dark_blue"),
                new PrideTagsColour("Purple", "dark_purple"),
                new PrideTagsColour("LightPurple", "light_purple"),
                new PrideTagsColour("Green", "green"),
                new PrideTagsColour("Yellow", "yellow"),
                new PrideTagsColour("Gold", "gold"),
                new PrideTagsColour("Black", "black"),
                new PrideTagsColour("White", "white")
        };
    }

    public static PrideTagsColourConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (COLOURS_CONFIG_FILE.exists()) {
            LOGGER.info("[{}] Loading teams config from {}", Pridetags.MOD_ID, COLOURS_CONFIG_FILE.getAbsolutePath());
            try (FileReader reader = new FileReader(COLOURS_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, PrideTagsColourConfig.class);
            } catch (Exception e) {
                LOGGER.error("[{}] Failed to load teams config", Pridetags.MOD_ID, e);
                INSTANCE = new PrideTagsColourConfig();
                save();
            }
        } else {
            LOGGER.info("[{}] No teams config found, using defaults", Pridetags.MOD_ID);
            INSTANCE = new PrideTagsColourConfig();
            save();
        }
    }

    public static void save() {
        try {
            if (!COLOURS_CONFIG_FILE.getParentFile().exists()) {
                LOGGER.info("[{}] Creating config directory {}", Pridetags.MOD_ID, COLOURS_CONFIG_FILE.getParentFile().getAbsolutePath());
                boolean mkdirs = COLOURS_CONFIG_FILE.getParentFile().mkdirs();
                if (!mkdirs) LOGGER.warn("[{}] Failed to create config directory {}", Pridetags.MOD_ID, COLOURS_CONFIG_FILE.getParentFile().getAbsolutePath());
            }
            try (FileWriter writer = new FileWriter(COLOURS_CONFIG_FILE)) {
                LOGGER.info("[{}] Saving teams config to {}", Pridetags.MOD_ID, COLOURS_CONFIG_FILE.getAbsolutePath());
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to save teams config", Pridetags.MOD_ID, e);
        }
    }

    public PrideTagsColour[] getColours() {
        return colours;
    }
}
