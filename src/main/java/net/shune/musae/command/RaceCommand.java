package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.race.RaceManager;
import net.shune.musae.race.RaceType;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RaceCommand {

    private static final SuggestionProvider<CommandSourceStack> RACE_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(RaceType.values()).map(RaceType::getId).collect(Collectors.toList()),
                    builder);

    public static LiteralArgumentBuilder<CommandSourceStack> buildRaceSubcommand() {
        return Commands.literal("racas")
                .requires(MusaePermissoes::admin)
                .then(Commands.argument("jogador", EntityArgument.player())
                        .then(Commands.argument("raca", StringArgumentType.word())
                                .suggests(RACE_SUGGESTIONS)
                                .executes(RaceCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        String raceId = StringArgumentType.getString(ctx, "raca");
        RaceType race = RaceType.fromId(raceId);

        if (race == null) {
            ctx.getSource().sendFailure(Component.literal("Raça inválida: " + raceId)
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        RaceManager.setRace(target, race);

        ctx.getSource().sendSuccess(() -> Component.literal("Raça de " + target.getGameProfile().getName()
                + " definida para: " + race.getDisplayName()).withStyle(ChatFormatting.GREEN), true);

        return 1;
    }
}