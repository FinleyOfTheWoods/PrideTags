package uk.co.finleyofthewoods.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.pridetags.Pridetags;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsPronouns;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsTeam;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsTeamsConfig;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsPronounsConfig;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class PrideTagsTeamCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrideTagsTeamCommand.class);
    private static final PrideTagsTeamsConfig CONFIG = PrideTagsTeamsConfig.get();
    private static final PrideTagsPronounsConfig PRONOUNS_CONFIG = PrideTagsPronounsConfig.get();
    private static final Formatting DEFAULT_COLOUR = Formatting.WHITE;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LOGGER.info("[{}] Registering command `/pridetag colour`", Pridetags.MOD_ID);
        dispatcher.register(literal("pridetag")
                .then(literal("colour")
                        .then(argument("colour", string())
                                .suggests(PrideTagsTeamCommand::suggestColours)
                                .executes(PrideTagsTeamCommand::executeSetColour)))
                .then(literal("pronouns")
                        .then(argument("pronouns", string())
                                .suggests(PrideTagsTeamCommand::suggestPronouns)
                                .executes(PrideTagsTeamCommand::executeSetPronouns))));
    }

    private static CompletableFuture<Suggestions> suggestColours(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        PrideTagsTeam[] teams = PrideTagsTeamsConfig.get().getTeams();
        LOGGER.debug("[{}] Suggesting colours {}", Pridetags.MOD_ID, Arrays.toString(teams));
        for (PrideTagsTeam team : teams) {
            builder.suggest(team.getName());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPronouns(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        PrideTagsPronouns[] pronouns = PrideTagsPronounsConfig.get().getPronouns();
        LOGGER.debug("[{}] Suggesting pronouns {}", Pridetags.MOD_ID, Arrays.toString(pronouns));
        for (PrideTagsPronouns pronoun : pronouns) {
            builder.suggest(pronoun.getName());
        }
        return builder.buildFuture();
    }

    private static int executeSetColour(CommandContext<ServerCommandSource> context) {
        try {
            LOGGER.info("[{}] Executing command `/pridetag` colour", Pridetags.MOD_ID);
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!player.isPlayer()) {
                LOGGER.warn("[{}] Command `/pridetag colour` executed by non-player entity", Pridetags.MOD_ID);
                source.sendFeedback(() -> Text.literal("You must be a player to use this command"), false);
                return 0;
            }

            String colour = getString(context, "colour");
            PrideTagsTeam[] teams = CONFIG.getTeams();
            PrideTagsTeam selectedColour = null;

            for (PrideTagsTeam team : teams) {
                LOGGER.debug("[{}] Checking team {}", Pridetags.MOD_ID, team.getName());
                if (team.getName().equalsIgnoreCase(colour)){
                    LOGGER.info("[{}] Player {}, Selected teams {}", Pridetags.MOD_ID, player.getName().getString(), team.getName());
                    selectedColour = team;
                    break;
                }
            }
            if (selectedColour == null || selectedColour.isEmpty()) {
                LOGGER.warn("[{}] Invalid colour {}", Pridetags.MOD_ID, colour);
                source.sendFeedback(() -> Text.literal("Invalid colour"), false);
                return 0;
            }

            String teamName = "pridetag_" + player.getUuidAsString();
            Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
            Team currentTeam = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());

            if (currentTeam != null && currentTeam.getName().equals(teamName)) {
                LOGGER.info("[{}] Player {} already in team {}, updating colour to: {}", Pridetags.MOD_ID, player.getName().getString(), teamName, selectedColour.getColour());
                currentTeam.setColor(getFormattingFromString(selectedColour.getColour()));
                return 1;
            }
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                LOGGER.info("[{}] Team does not exist. Creating team {}", Pridetags.MOD_ID, selectedColour.getName());
                team = scoreboard.addTeam(teamName);
                team.setColor(getFormattingFromString(selectedColour.getColour()));
            }
            scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
            return 1;
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to execute command", Pridetags.MOD_ID, e);
            return 0;
        }
    }

    private static int executeSetPronouns(CommandContext<ServerCommandSource> context) {
        try {
            LOGGER.info("[{}] Executing `/pridetag pronouns` command", Pridetags.MOD_ID);
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!player.isPlayer()) {
                LOGGER.warn("[{}] Command `/pridetag pronouns` executed by non-player entity", Pridetags.MOD_ID);
                source.sendFeedback(() -> Text.literal("You must be a player to use this command"), false);
            }

            String pronouns = getString(context, "pronouns");
            PrideTagsPronouns[] pronounsList = PRONOUNS_CONFIG.getPronouns();
            PrideTagsPronouns selectedPronouns = null;

            for (PrideTagsPronouns pronoun : pronounsList) {
                LOGGER.debug("[{}] Checking pronoun {}", Pridetags.MOD_ID, pronoun.getName());
                if (pronoun.getName().equalsIgnoreCase(pronouns)) {
                    LOGGER.info("[{}] Player {}, Selected pronoun {}", Pridetags.MOD_ID, player.getName().getString(), pronoun.getName());
                    selectedPronouns = pronoun;
                    break;
                }
            }

            if (selectedPronouns == null) {
                LOGGER.warn("[{}] Invalid pronouns {}", Pridetags.MOD_ID, pronouns);
                source.sendFeedback(() -> Text.literal("Invalid pronouns"), false);
                return 0;
            }

            Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
            Team currentTeam = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());

            if (currentTeam != null) {
                String currentPronouns = currentTeam.getSuffix().getString();
                if (currentPronouns != null && currentPronouns.equals(selectedPronouns.getDisplay())) {
                    LOGGER.info("[{}] Player {} already in team {}, with same pronouns: {}. Skipping.", Pridetags.MOD_ID, player.getName().getString(), currentTeam.getName(), selectedPronouns.getDisplay());
                    return 0;
                }
                LOGGER.info("[{}] Player {} already in team {}, updating pronouns to: {}", Pridetags.MOD_ID, player.getName().getString(), currentTeam.getName(), selectedPronouns.getDisplay());
                currentTeam.setSuffix(Text.literal(selectedPronouns.getDisplay()));
                return 1;
            }

            LOGGER.info("[{}] Player {} not in team. Creating new team.", Pridetags.MOD_ID, player.getName().getString());
            String teamName = "pride_" + player.getUuidAsString();
            Team team = scoreboard.getTeam(teamName);

            if (team == null) {
                LOGGER.info("[{}] Team does not exist. Creating team {}]", Pridetags.MOD_ID, teamName);
                team = scoreboard.addTeam(teamName);
            }

            team.setSuffix(Text.literal(selectedPronouns.getDisplay()));
            team.setColor(DEFAULT_COLOUR);
            return 1;
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to execute command", Pridetags.MOD_ID, e);
            return 0;
        }
    }

    private static Formatting getFormattingFromString(String colour) {
        return switch (colour.toLowerCase()) {
            case "red" -> Formatting.RED;
            case "dark_red" -> Formatting.DARK_RED;
            case "aqua" -> Formatting.AQUA;
            case "dark_aqua" -> Formatting.DARK_AQUA;
            case "blue" -> Formatting.BLUE;
            case "dark_blue" -> Formatting.DARK_BLUE;
            case "green" -> Formatting.GREEN;
            case "yellow" -> Formatting.YELLOW;
            case "gold" -> Formatting.GOLD;
            case "purple" -> Formatting.DARK_PURPLE;
            case "light_purple" -> Formatting.LIGHT_PURPLE;
            case "black" -> Formatting.BLACK;
            default -> DEFAULT_COLOUR;
        };
    }
}
