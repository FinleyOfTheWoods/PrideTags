package uk.co.finleyofthewoods.pridetags.config;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.pridetags.Pridetags;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class PrideTagsTeamsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrideTagsTeamsConfig.class);
    private static PrideTagsTeamsConfig INSTANCE;
    private static final Gson GSON = new Gson();
    private static final File TEAMS_CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pride_tags/teams.json").toFile();

    private final PrideTagsTeam[] teams;

    public PrideTagsTeamsConfig() {
        LOGGER.info("[{}] Loading default config", Pridetags.MOD_ID);
        this.teams = new PrideTagsTeam[]{
            new PrideTagsTeam("red", "red", "", ""),
            new PrideTagsTeam("dark_red", "dark_red", "", ""),
            new PrideTagsTeam("light_blue", "aqua", "", ""),
            new PrideTagsTeam("blue", "blue", "", ""),
            new PrideTagsTeam("purple", "dark_purple", "", ""),
            new PrideTagsTeam("light_purple", "light_purple", "", "")
        };
    }

    public static PrideTagsTeamsConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (TEAMS_CONFIG_FILE.exists()) {
            LOGGER.info("[{}] Loading teams config from {}", Pridetags.MOD_ID, TEAMS_CONFIG_FILE.getAbsolutePath());
            try (FileReader reader = new FileReader(TEAMS_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, PrideTagsTeamsConfig.class);
            } catch (Exception e) {
                LOGGER.error("[{}] Failed to load teams config", Pridetags.MOD_ID, e);
                INSTANCE = new PrideTagsTeamsConfig();
                save();
            }
        } else {
            LOGGER.info("[{}] No teams config found, using defaults", Pridetags.MOD_ID);
            INSTANCE = new PrideTagsTeamsConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(TEAMS_CONFIG_FILE)) {
            LOGGER.info("[{}] Saving teams config to {}", Pridetags.MOD_ID, TEAMS_CONFIG_FILE.getAbsolutePath());
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to save teams config", Pridetags.MOD_ID, e);
        }
    }

    public PrideTagsTeam[] getTeams() {
        return teams;
    }
}
