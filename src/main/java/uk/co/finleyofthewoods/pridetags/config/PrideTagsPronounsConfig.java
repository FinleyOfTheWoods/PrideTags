package uk.co.finleyofthewoods.pridetags.config;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.pridetags.Pridetags;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PrideTagsPronounsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrideTagsPronounsConfig.class);
    private static PrideTagsPronounsConfig INSTANCE;
    private static final Gson GSON = new Gson();
    private static final File PRONOUNS_CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pride_tags/pronouns.json").toFile();

    private final PrideTagsPronouns[] pronouns;

    public PrideTagsPronounsConfig() {
        LOGGER.info("[{}] Loading default pronouns config", Pridetags.MOD_ID);
        this.pronouns = new PrideTagsPronouns[]{
                new PrideTagsPronouns("they_them", " [They/Them]"),
                new PrideTagsPronouns("he_him", " [He/Him]"),
                new PrideTagsPronouns("she_her", " [She/Her]"),
                new PrideTagsPronouns("she_they", " [She/They]"),
                new PrideTagsPronouns("he_they", " [He/They]")
        };
    }

    public static PrideTagsPronounsConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (PRONOUNS_CONFIG_FILE.exists()) {
            LOGGER.info("[{}] Loading pronouns config from {}", Pridetags.MOD_ID, PRONOUNS_CONFIG_FILE.getAbsolutePath());
            try (FileReader reader = new FileReader(PRONOUNS_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, PrideTagsPronounsConfig.class);
            } catch (IOException e) {
                LOGGER.error("[{}] Failed to load pronouns config. Loading defaults.", Pridetags.MOD_ID, e);
                INSTANCE = new PrideTagsPronounsConfig();
                save();
            }
        } else {
            LOGGER.info("[{}] No pronouns config found. Loading defaults.", Pridetags.MOD_ID);
            INSTANCE = new PrideTagsPronounsConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(PRONOUNS_CONFIG_FILE)) {
            LOGGER.info("[{}] Saving pronouns config to {}", Pridetags.MOD_ID, PRONOUNS_CONFIG_FILE.getAbsolutePath());
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            LOGGER.error("[{}] Failed to save pronouns config.", Pridetags.MOD_ID, e);
        }
    }

    public PrideTagsPronouns[] getPronouns() {
        return pronouns;
    }
}
