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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.finleyofthewoods.pridetags.Pridetags;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsPronouns;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsColour;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsColourConfig;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsPronounsConfig;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class PrideTagsTeamCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrideTagsTeamCommand.class);
    private static final PrideTagsColourConfig CONFIG = PrideTagsColourConfig.get();
    private static final PrideTagsPronounsConfig PRONOUNS_CONFIG = PrideTagsPronounsConfig.get();
    private static final Formatting DEFAULT_COLOUR = Formatting.WHITE;
    private static final String PRONOUNS_PREFIX = " • ";
    private static final String TEAM_PREFIX = "pride_tag_";

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
                                .executes(PrideTagsTeamCommand::executeSetPronouns)))
                .then(literal("reset")
                        .then(literal("reset"))
                        .executes(PrideTagsTeamCommand::executeReset)));
    }

    private static CompletableFuture<Suggestions> suggestColours(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        PrideTagsColour[] teams = PrideTagsColourConfig.get().getColours();
        String value = "";
        try {
            value = getString(context, "colour");
        } catch (Exception e) {
            LOGGER.debug("[{}] Failed to get pronouns value", Pridetags.MOD_ID, e);
        }
        LOGGER.debug("[{}] Suggesting colours {} for value of {}", Pridetags.MOD_ID, Arrays.toString(teams), value);
        for (PrideTagsColour team : teams) {
            if (!team.name().toLowerCase().contains(value.toLowerCase())) continue;
            builder.suggest(team.name());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPronouns(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        PrideTagsPronouns[] pronouns = PrideTagsPronounsConfig.get().getPronouns();
        String value = "";
        try {
            value = getString(context, "pronouns");
        } catch (Exception e) {
            LOGGER.debug("[{}] Failed to get pronouns value", Pridetags.MOD_ID, e);
        }

        LOGGER.debug("[{}] Suggesting pronouns {} for value of {}", Pridetags.MOD_ID, Arrays.toString(pronouns), value);
        for (PrideTagsPronouns pronoun : pronouns) {
            if (!pronoun.name().toLowerCase().contains(value.toLowerCase())) continue;
            builder.suggest(pronoun.name());
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
            PrideTagsColour[] teams = CONFIG.getColours();
            PrideTagsColour selectedColour = null;

            for (PrideTagsColour team : teams) {
                LOGGER.debug("[{}] Checking team {}", Pridetags.MOD_ID, team.name());
                if (team.name().equalsIgnoreCase(colour)){
                    LOGGER.info("[{}] Player {}, Selected teams {}", Pridetags.MOD_ID, player.getName().getString(), team.name());
                    selectedColour = team;
                    break;
                }
            }
            if (selectedColour == null || selectedColour.isEmpty()) {
                LOGGER.warn("[{}] Invalid colour {}", Pridetags.MOD_ID, colour);
                source.sendFeedback(() -> Text.literal("Invalid colour"), false);
                return 0;
            }

            String teamName = TEAM_PREFIX + player.getUuidAsString();
            Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
            final Team currentTeam = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());

            if (currentTeam != null && currentTeam.getName().equals(teamName)) {
                LOGGER.info("[{}] Player {} already in team {}, updating colour to: {}", Pridetags.MOD_ID, player.getName().getString(), teamName, selectedColour.colour());
                currentTeam.setColor(getFormattingFromString(selectedColour.colour()));
                source.sendFeedback(() -> Text.literal("Pronouns set. Name will appear as: " + currentTeam.getDisplayName().getString()), false);

                return 1;
            }
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                LOGGER.info("[{}] Team does not exist. Creating team {}", Pridetags.MOD_ID, selectedColour.name());
                team = scoreboard.addTeam(teamName);
                team.setColor(getFormattingFromString(selectedColour.colour()));
            }
            scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
            Team finalTeam = team;
            source.sendFeedback(() -> Text.literal("Colour set. Name will appear as: " + finalTeam.getDisplayName().getString()), false);

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
                LOGGER.debug("[{}] Checking pronoun {}", Pridetags.MOD_ID, pronoun.name());
                if (pronoun.name().equalsIgnoreCase(pronouns)) {
                    LOGGER.info("[{}] Player {}, Selected pronoun {}", Pridetags.MOD_ID, player.getName().getString(), pronoun.name());
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
            final Team currentTeam = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());

            if (currentTeam != null) {
                String currentPronouns = currentTeam.getSuffix().getString();
                if (currentPronouns != null && currentPronouns.equals(selectedPronouns.display())) {
                    LOGGER.info("[{}] Player {} already in team {}, with same pronouns: {}. Skipping.",
                            Pridetags.MOD_ID, player.getName().getString(), currentTeam.getName(), selectedPronouns.display());
                    return 0;
                }
                LOGGER.info("[{}] Player {} already in team {}, updating pronouns to: {}",
                        Pridetags.MOD_ID, player.getName().getString(), currentTeam.getName(), selectedPronouns.display());
                currentTeam.setSuffix(Text.literal(PRONOUNS_PREFIX + selectedPronouns.display()).formatted(Formatting.GRAY));
                source.sendFeedback(() -> Text.literal("Pronouns set. Name will appear as: " + currentTeam.getDisplayName().getString()), false);

                return 1;
            }

            LOGGER.info("[{}] Player {} not in team. Creating new team.", Pridetags.MOD_ID, player.getName().getString());
            String teamName = TEAM_PREFIX + player.getUuidAsString();
            Team team = scoreboard.getTeam(teamName);

            if (team == null) {
                LOGGER.info("[{}] Team does not exist. Creating team {}]", Pridetags.MOD_ID, teamName);
                team = scoreboard.addTeam(teamName);
            }

            team.setSuffix(Text.literal(PRONOUNS_PREFIX + selectedPronouns.display()).formatted(Formatting.GRAY));
            team.setColor(DEFAULT_COLOUR);
            scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
            Team finalTeam = team;
            source.sendFeedback(() -> Text.literal("Pronouns set. Name will appear as: " + finalTeam.getDisplayName().getString()), false);
            return 1;
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to execute command", Pridetags.MOD_ID, e);
            return 0;
        }
    }

    private static int executeReset(CommandContext<ServerCommandSource> context) {
        try {
            LOGGER.info("[{}] Executing `/pridetag reset` command", Pridetags.MOD_ID);
            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!player.isPlayer()) {
                LOGGER.warn("[{}] Command `/pridetag pronouns` executed by non-player entity", Pridetags.MOD_ID);
                source.sendFeedback(() -> Text.literal("You must be a player to use this command"), false);
            }

            Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
            Team currentTeam = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());

            if (currentTeam != null) {
                LOGGER.info("[{}] Player {} already in team {}, resetting to: None",
                        Pridetags.MOD_ID, player.getName().getString(), currentTeam.getName());
                currentTeam.setColor(DEFAULT_COLOUR);
                currentTeam.setSuffix(Text.literal(""));
                return 1;
            } else {
                LOGGER.info("[{}] Player {} not in team. Skipping.", Pridetags.MOD_ID, player.getName().getString());
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to execute command", Pridetags.MOD_ID, e);
            return 0;
        }
    }

    private static Formatting getFormattingFromString(@NotNull String colour) {
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
            case "dark_purple" -> Formatting.DARK_PURPLE;
            case "light_purple" -> Formatting.LIGHT_PURPLE;
            case "black" -> Formatting.BLACK;
            case "white" -> Formatting.WHITE;
            default -> DEFAULT_COLOUR;
        };
    }
}
