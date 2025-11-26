package uk.co.finleyofthewoods.pridetags;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.commands.PrideTagsTeamCommand;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsColourConfig;

public class Pridetags implements ModInitializer {
    public static final String MOD_ID = "pridetags";
    public static final String MOD_NAME = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(modContainer -> modContainer.getMetadata().getName())
            .orElse(Pridetags.class.getSimpleName());
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    private static final Logger LOGGER = LoggerFactory.getLogger(Pridetags.class);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] {} initalising...", MOD_ID, MOD_NAME);
        LOGGER.info("[{}] Version: {}", MOD_ID, MOD_VERSION);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("[{}] Loading config...", MOD_ID);
            PrideTagsColourConfig.load();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[{}] Saving config...", MOD_ID);
            PrideTagsColourConfig.save();
        });

        CommandRegistrationCallback.EVENT.register(PrideTagsTeamCommand::register);

        LOGGER.info("[{}] {} {} initialised!", MOD_ID, MOD_NAME, MOD_VERSION);
    }
}
