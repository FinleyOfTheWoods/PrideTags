package uk.co.finleyofthewoods.pridetags.config;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class PrideTagsPronounsConfig {
    private static PrideTagsPronounsConfig INSTANCE;
    private static final Gson GSON = new Gson();
    private static final File PRONOUNS_CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("pride_tags/pronouns.json").toFile();

    private final PrideTagsPronouns[] pronouns;

    public PrideTagsPronounsConfig() {
        log.info("Loading default pronouns config");
        this.pronouns = new PrideTagsPronouns[]{
                new PrideTagsPronouns("they_them", "They/Them"),
                new PrideTagsPronouns("he_him", "He/Him"),
                new PrideTagsPronouns("she_her", "She/Her"),
                new PrideTagsPronouns("she_they", "She/They"),
                new PrideTagsPronouns("he_they", "He/They"),
                new PrideTagsPronouns("they_he", "They/He"),
                new PrideTagsPronouns("they_she", "They/She"),
                new PrideTagsPronouns("he_she", "He/She"),
                new PrideTagsPronouns("it_its", "It/Its"),
                new PrideTagsPronouns("any", "Any pronouns"),
                new PrideTagsPronouns("no_pronouns", "No Pronouns"),
                new PrideTagsPronouns("name_only", "Name only"),
                new PrideTagsPronouns("neopronouns", "Neopronouns")
        };
    }

    public static PrideTagsPronounsConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (PRONOUNS_CONFIG_FILE.exists()) {
            log.info("Loading pronouns config from {}", PRONOUNS_CONFIG_FILE.getAbsolutePath());
            try (FileReader reader = new FileReader(PRONOUNS_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, PrideTagsPronounsConfig.class);
            } catch (IOException e) {
                log.error("Failed to load pronouns config. Loading defaults.", e);
                INSTANCE = new PrideTagsPronounsConfig();
                save();
            }
        } else {
            log.info("No pronouns config found. Loading defaults.");
            INSTANCE = new PrideTagsPronounsConfig();
            save();
        }
    }

    public static void save() {
        try {
            if (!PRONOUNS_CONFIG_FILE.getParentFile().exists()) {
                log.info("Creating config directory {}", PRONOUNS_CONFIG_FILE.getParentFile().getAbsolutePath());
                boolean mkdirs = PRONOUNS_CONFIG_FILE.getParentFile().mkdirs();
                if (!mkdirs) log.warn("Failed to create config directory {}", PRONOUNS_CONFIG_FILE.getParentFile().getAbsolutePath());
            }
            try (FileWriter writer = new FileWriter(PRONOUNS_CONFIG_FILE)) {
                log.info("Saving pronouns config to {}", PRONOUNS_CONFIG_FILE.getAbsolutePath());
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            log.error("Failed to save pronouns config.", e);
        }
    }

    public List<PrideTagsPronouns> getPronouns() {
        return Arrays.stream(pronouns).toList();
    }
}
