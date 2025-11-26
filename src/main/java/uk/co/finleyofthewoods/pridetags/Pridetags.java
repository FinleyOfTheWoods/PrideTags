package uk.co.finleyofthewoods.pridetags;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.commands.PrideTagsTeamCommand;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsTeamsConfig;

public class Pridetags implements ModInitializer {
    public static final String MOD_ID = "pridetags";
    public static final String MOD_NAME = "Pride Tags";
    public static final String MOD_VERSION = "1.0.0";
    private static final Logger LOGGER = LoggerFactory.getLogger(Pridetags.class);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] {} initalising...", MOD_ID, MOD_NAME);
        LOGGER.info("[{}] Version: {}", MOD_ID, MOD_VERSION);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("[{}] Loading config...", MOD_ID);
            PrideTagsTeamsConfig.load();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[{}] Saving config...", MOD_ID);
            PrideTagsTeamsConfig.save();
        });

        CommandRegistrationCallback.EVENT.register(PrideTagsTeamCommand::register);

        LOGGER.info("[{}] {} {} initialised!", MOD_ID, MOD_NAME, MOD_VERSION);
    }
}
