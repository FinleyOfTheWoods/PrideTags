package uk.co.finleyofthewoods.pridetags.config;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Slf4j
public class PrideTagsColourConfig {
    private static PrideTagsColourConfig INSTANCE;
    private static final Gson GSON = new Gson();
    private static final File COLOURS_CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pride_tags/teams.json").toFile();

    private final PrideTagsColour[] colours;

    public PrideTagsColourConfig() {
        log.info("Loading default config");
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
            log.info("Loading teams config from {}", COLOURS_CONFIG_FILE.getAbsolutePath());
            try (FileReader reader = new FileReader(COLOURS_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, PrideTagsColourConfig.class);
            } catch (Exception e) {
                log.error("Failed to load teams config", e);
                INSTANCE = new PrideTagsColourConfig();
                save();
            }
        } else {
            log.info("No teams config found, using defaults");
            INSTANCE = new PrideTagsColourConfig();
            save();
        }
    }

    public static void save() {
        try {
            if (!COLOURS_CONFIG_FILE.getParentFile().exists()) {
                log.info("Creating config directory {}", COLOURS_CONFIG_FILE.getParentFile().getAbsolutePath());
                boolean mkdirs = COLOURS_CONFIG_FILE.getParentFile().mkdirs();
                if (!mkdirs) log.warn("Failed to create config directory {}", COLOURS_CONFIG_FILE.getParentFile().getAbsolutePath());
            }
            try (FileWriter writer = new FileWriter(COLOURS_CONFIG_FILE)) {
                log.info("Saving teams config to {}", COLOURS_CONFIG_FILE.getAbsolutePath());
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            log.error("Failed to save teams config", e);
        }
    }

    public PrideTagsColour[] getColours() {
        return colours;
    }
}
