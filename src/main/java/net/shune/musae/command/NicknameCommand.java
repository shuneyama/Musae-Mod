package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.compatibility.TabModCompat;
import net.shune.musae.nickname.NicknameData;
import net.shune.musae.nickname.NicknameGradientUtil;

import java.util.*;

public class NicknameCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildNicknameSubcommand() {
        return Commands.literal("nickname")
                .requires(MusaePermissoes::admin)
                .then(buildAlterarSubcommand())
                .then(buildColorirSubcommand())
                .then(buildListaSubcommand())
                .then(buildRemoverSubcommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAlterarSubcommand() {
        return Commands.literal("alterar")
                .then(Commands.argument("jogador", EntityArgument.player())
                        .then(Commands.argument("nick", StringArgumentType.greedyString())
                                .executes(NicknameCommand::executeAlterar)
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildColorirSubcommand() {
        return Commands.literal("colorir")
                .then(Commands.argument("jogador", EntityArgument.player())
                        .then(Commands.argument("cor", StringArgumentType.string())
                                .executes(NicknameCommand::executeColorir)
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildListaSubcommand() {
        return Commands.literal("lista")
                .executes(NicknameCommand::executeLista);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRemoverSubcommand() {
        return Commands.literal("remover")
                .then(Commands.argument("jogador", EntityArgument.player())
                        .executes(NicknameCommand::executeRemover)
                );
    }

    private static int executeAlterar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        String rawNick = StringArgumentType.getString(ctx, "nick").trim();

        if (rawNick.startsWith("\"") && rawNick.endsWith("\"") && rawNick.length() > 1) {
            rawNick = rawNick.substring(1, rawNick.length() - 1).trim();
        } else if (rawNick.startsWith("\"")) {
            rawNick = rawNick.substring(1).trim();
        }

        if (rawNick.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cO nickname não pode ser vazio!"));
            return 0;
        }

        if (rawNick.length() > 24) {
            ctx.getSource().sendFailure(Component.literal("§cO nickname não pode ter mais de 24 caracteres!"));
            return 0;
        }

        NicknameData data = NicknameData.get(ctx.getSource().getServer());

        if (data.isNicknameTaken(rawNick, target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§cO nickname §e" + rawNick + "§c já está em uso!"));
            return 0;
        }

        NicknameData.NicknameEntry existing = data.getNickname(target.getUUID());
        List<String> colors = existing != null ? existing.colors() : List.of();

        data.setNickname(target.getUUID(), rawNick, colors);

        data.syncToAll(ctx.getSource().getServer(), target.getUUID());

        refreshTabList(target);
        refreshCommandTree(ctx.getSource().getServer());
        TabModCompat.syncToTab(target, data);

        MutableComponent styledNick = data.getStyledNickname(target.getUUID());
        MutableComponent feedback = Component.literal("§aNickname de §b" + target.getName().getString() + "§a alterado para ");
        if (styledNick != null) {
            feedback.append(styledNick);
        }
        feedback.append(Component.literal("§a."));
        ctx.getSource().sendSuccess(() -> feedback, true);

        return 1;
    }

    private static int executeColorir(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        String hexInput = StringArgumentType.getString(ctx, "cor").trim();

        try {
            NicknameGradientUtil.parseHex(hexInput);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("§cCor inválida: §e" + hexInput + "§c. Use o formato #RRGGBB."));
            return 0;
        }

        NicknameData data = NicknameData.get(ctx.getSource().getServer());

        NicknameData.NicknameEntry existing = data.getNickname(target.getUUID());
        if (existing == null) {
            ctx.getSource().sendFailure(Component.literal("§c" + target.getName().getString()
                    + " não possui um nickname. Use §e/shune nickname alterar§c primeiro."));
            return 0;
        }

        data.setNickname(target.getUUID(), existing.nickname(), List.of(hexInput));

        data.syncToAll(ctx.getSource().getServer(), target.getUUID());

        refreshTabList(target);
        TabModCompat.syncToTab(target, data);

        int colorInt = NicknameGradientUtil.parseHex(hexInput);
        MutableComponent styledNick = data.getStyledNickname(target.getUUID());
        MutableComponent feedback = Component.literal("§aCor do nickname de §b" + target.getName().getString() + "§a alterada para ");
        if (styledNick != null) {
            feedback.append(styledNick);
        }
        feedback.append(Component.literal(" §8("))
                .append(Component.literal(hexInput).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorInt))))
                .append(Component.literal("§8)§a."));
        ctx.getSource().sendSuccess(() -> feedback, true);

        return 1;
    }

    private static int executeLista(CommandContext<CommandSourceStack> ctx) {
        NicknameData data = NicknameData.get(ctx.getSource().getServer());
        Map<UUID, NicknameData.NicknameEntry> all = data.getAllNicknames();

        if (all.isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("§eNenhum nickname registrado."),
                    false
            );
            return 1;
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("§6§l=== Nicknames Registrados (" + all.size() + ") ==="),
                false
        );

        for (Map.Entry<UUID, NicknameData.NicknameEntry> entry : all.entrySet()) {
            UUID uuid = entry.getKey();
            NicknameData.NicknameEntry nickEntry = entry.getValue();

            ServerPlayer onlinePlayer = ctx.getSource().getServer().getPlayerList().getPlayer(uuid);
            String realName = onlinePlayer != null ? onlinePlayer.getGameProfile().getName() : uuid.toString();

            MutableComponent styledNick = data.getStyledNickname(uuid);
            MutableComponent line = Component.literal("§7- §b" + realName + " §7→ ");
            if (styledNick != null) {
                line.append(styledNick);
            }

            if (!nickEntry.colors().isEmpty()) {
                int colorInt = NicknameGradientUtil.parseHex(nickEntry.colors().get(0));
                line.append(Component.literal(" §8["))
                        .append(Component.literal(nickEntry.colors().get(0))
                                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorInt))))
                        .append(Component.literal("§8]"));
            }

            ctx.getSource().sendSuccess(() -> line, false);
        }

        return 1;
    }

    private static int executeRemover(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jogador");
        NicknameData data = NicknameData.get(ctx.getSource().getServer());

        NicknameData.NicknameEntry existing = data.getNickname(target.getUUID());
        if (existing == null) {
            ctx.getSource().sendFailure(Component.literal("§c" + target.getName().getString() + " não possui um nickname."));
            return 0;
        }

        String oldNick = existing.nickname();
        data.removeNickname(target.getUUID());

        data.syncToAll(ctx.getSource().getServer(), target.getUUID());

        refreshTabList(target);
        refreshCommandTree(ctx.getSource().getServer());
        TabModCompat.syncToTab(target, data);

        ctx.getSource().sendSuccess(
                () -> Component.literal("§aNickname §e" + oldNick + "§a removido de §b" + target.getName().getString() + "§a."),
                true
        );

        return 1;
    }

    private static void refreshCommandTree(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            server.getCommands().sendCommands(player);
        }
    }

    private static void refreshTabList(ServerPlayer target) {
        target.refreshDisplayName();
        target.refreshTabListName();
        net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket packet =
                new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                        net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        target
                );
        for (ServerPlayer player : target.getServer().getPlayerList().getPlayers()) {
            player.connection.send(packet);
        }
    }
}