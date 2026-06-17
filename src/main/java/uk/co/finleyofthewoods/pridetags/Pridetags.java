package uk.co.finleyofthewoods.pridetags;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import uk.co.finleyofthewoods.pridetags.commands.PrideTagsTeamCommand;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsColourConfig;

@Slf4j
public class Pridetags implements ModInitializer {
    public static final String MOD_ID = "pridetags";
    public static final String MOD_NAME = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(modContainer -> modContainer.getMetadata().getName())
            .orElse(Pridetags.class.getSimpleName());
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

    @Override
    public void onInitialize() {
        log.info("[{}] {} initalising...", MOD_ID, MOD_NAME);
        log.info("[{}] Version: {}", MOD_ID, MOD_VERSION);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            log.info("[{}] Loading config...", MOD_ID);
            PrideTagsColourConfig.load();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            log.info("[{}] Saving config...", MOD_ID);
            PrideTagsColourConfig.save();
        });

        CommandRegistrationCallback.EVENT.register(PrideTagsTeamCommand::register);

        log.info("[{}] {} {} initialised!", MOD_ID, MOD_NAME, MOD_VERSION);
    }
}
