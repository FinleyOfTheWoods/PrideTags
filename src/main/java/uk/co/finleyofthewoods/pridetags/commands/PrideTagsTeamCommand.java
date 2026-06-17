package uk.co.finleyofthewoods.pridetags.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsColour;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsColourConfig;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsPronouns;
import uk.co.finleyofthewoods.pridetags.config.PrideTagsPronounsConfig;
import net.minecraft.commands.CommandSourceStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Slf4j
public class PrideTagsTeamCommand {
    private static final PrideTagsColourConfig CONFIG = PrideTagsColourConfig.get();
    private static final PrideTagsPronounsConfig PRONOUNS_CONFIG = PrideTagsPronounsConfig.get();
    private static final ChatFormatting DEFAULT_COLOUR = ChatFormatting.WHITE;
    private static final String PRONOUNS_PREFIX = " • ";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, CommandSelection selection) {
        log.info("Registering pride_tag command");
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

    private static CompletableFuture<Suggestions> suggestColours(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        PrideTagsColour[] teams = PrideTagsColourConfig.get().getColours();
        String value = "";
        try {
            value = getString(context, "colour");
        } catch (Exception e) {
            log.debug("Failed to get pronouns value", e);
        }
        log.debug("Suggesting colours {} for value of {}", Arrays.toString(teams), value);
        for (PrideTagsColour team : teams) {
            if (!team.name().toLowerCase().contains(value.toLowerCase())) continue;
            builder.suggest(team.name());
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPronouns(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        PrideTagsPronouns[] pronouns = PrideTagsPronounsConfig.get().getPronouns().toArray(new PrideTagsPronouns[0]);
        String value = "";
        try {
            value = getString(context, "pronouns");
        } catch (Exception e) {
            log.debug("Failed to get pronouns value", e);
        }

        log.debug("Suggesting pronouns {} for value of {}", Arrays.toString(pronouns), value);
        for (PrideTagsPronouns pronoun : pronouns) {
            if (!pronoun.name().toLowerCase().contains(value.toLowerCase())) continue;
            builder.suggest(pronoun.name());
        }
        return builder.buildFuture();
    }

    private static int executeSetColour(CommandContext<CommandSourceStack> context) {
        try {
            log.debug("Executing command `/pridetag` colour");
            CommandSourceStack source = context.getSource();
            if (!source.isPlayer()) {
                log.warn("Command `/pridetag colour` executed by non-player entity");
                source.sendFailure(Component.literal("You must be a player to use this command"));
                return 1;
            }
            ServerPlayer player = source.getPlayerOrException();
            String colour = getString(context, "colour");
            List<PrideTagsColour> colours = Arrays.stream(CONFIG.getColours()).toList();

            PrideTagsColour selectedColour = colours.stream()
                    .filter(team -> team.name().equalsIgnoreCase(colour))
                    .findFirst()
                    .orElse(null);

            if (selectedColour == null || selectedColour.isEmpty()) {
                log.warn("Invalid colour {}", colour);
                player.sendSystemMessage(Component.literal("Invalid colour chosen"), true);
                return 0;
            }

            PlayerTeam team = getOrCreateTeam(player);
            team.setColor(getFormattingFromString(selectedColour.colour()));

            return 1;
        } catch (Exception e) {
            log.error("Failed to execute command", e);
            return 0;
        }
    }

    private static int executeSetPronouns(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            if (!source.isPlayer()) {
                log.warn("Command `/pridetag pronouns` executed by non-player entity");
                source.sendFailure(Component.literal("You must be a player to use this command"));
                return 1;
            }
            ServerPlayer player = source.getPlayerOrException();
            PlayerTeam team = getOrCreateTeam(player);

            String pronouns = getString(context, "pronouns");
            List<PrideTagsPronouns> pronounList = PRONOUNS_CONFIG.getPronouns();
            pronounList.stream()
                    .filter(p -> p.name().equalsIgnoreCase(pronouns))
                    .findFirst()
                    .ifPresent(selectedPronoun ->
                            team.setPlayerSuffix(Component.literal(PRONOUNS_PREFIX + selectedPronoun.display())
                                    .withStyle(ChatFormatting.GRAY)));
            return 0;
        } catch (Exception e) {
            log.error("Failed to execute command set pronouns", e);
            return 1;
        }
    }

    private static int executeReset(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            if (!source.isPlayer()) {
                log.warn("Command `/pridetag reset` executed by non-player entity");
                source.sendFailure(Component.literal("You must be a player to use this command"));
                return 1;
            }
            ServerPlayer player = source.getPlayerOrException();
            PlayerTeam team = player.getTeam();
            if (team == null) {
                player.sendSystemMessage(Component.literal("You do not have a team"), true);
                return 0;
            }
            team.setPlayerSuffix(Component.literal(""));
            team.setColor(DEFAULT_COLOUR);
            return 0;
        } catch (Exception e) {
            log.error("Failed to execute command reset", e);
            return 1;
        }
    }

    private static ChatFormatting getFormattingFromString(@NotNull String colour) {
        return switch (colour.toLowerCase()) {
            case "red" -> ChatFormatting.RED;
            case "dark_red" -> ChatFormatting.DARK_RED;
            case "aqua" -> ChatFormatting.AQUA;
            case "dark_aqua" -> ChatFormatting.DARK_AQUA;
            case "blue" -> ChatFormatting.BLUE;
            case "dark_blue" -> ChatFormatting.DARK_BLUE;
            case "green" -> ChatFormatting.GREEN;
            case "yellow" -> ChatFormatting.YELLOW;
            case "gold" -> ChatFormatting.GOLD;
            case "dark_purple" -> ChatFormatting.DARK_PURPLE;
            case "light_purple" -> ChatFormatting.LIGHT_PURPLE;
            case "black" -> ChatFormatting.BLACK;
            case "white" -> ChatFormatting.WHITE;
            default -> DEFAULT_COLOUR;
        };
    }

    private static PlayerTeam getOrCreateTeam(ServerPlayer player) {
        Scoreboard scoreboard = player.level().getScoreboard();
        String teamName = "pride_tags_" + player.getStringUUID();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        }
        if (!team.getName().equals(teamName)) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        }
        return team;
    }
}
