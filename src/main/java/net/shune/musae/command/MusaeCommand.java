package net.shune.musae.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.shune.musae.auth.AutorizacaoServidor;

public class MusaeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("musae")
                        .requires(src -> AutorizacaoServidor.isAutorizado())
                        .then(ScaleCommand.buildScaleSubcommand())
                        .then(NicknameCommand.buildNicknameSubcommand())
                        .then(RaceCommand.buildRaceSubcommand())
                        .then(SeletorCommand.buildSubcommand())
                        .then(MensagensCommand.buildMensagensSubcommand())
                        .then(DimensaoCommand.buildDimensaoSubcommand())
                        .then(MonitoramentoCommand.buildMonitoramentoSubcommand())
                        .then(AnotacaoCommand.buildAnotacaoSubcommand())
                        .then(ItensCommand.buildItensSubcommand())
                        .then(ChatCommand.buildChatSubcommand())
                        .then(BalaoCommand.buildBalaoSubcommand())
                        .then(VozCommand.buildVozSubcommand())
        );
    }
}