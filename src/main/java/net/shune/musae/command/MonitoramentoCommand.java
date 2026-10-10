package net.shune.musae.command;

import net.shune.musae.compatibility.MusaePermissoes;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.shune.musae.monitor.ConfiguracaoHorarioProibido;
import net.shune.musae.monitor.FormatoUtil;
import net.shune.musae.monitor.HistoricoJogador;
import net.shune.musae.monitor.HistoricoLoginManager;
import net.shune.musae.monitor.SessaoJogador;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;

public class MonitoramentoCommand {

    private static final int ITENS_POR_PAGINA = 10;
    private static final ChatFormatting COR_PAR = ChatFormatting.WHITE;
    private static final ChatFormatting COR_IMPAR = ChatFormatting.GRAY;

    public static LiteralArgumentBuilder<CommandSourceStack> buildMonitoramentoSubcommand() {
        return Commands.literal("monitoramento")
                .requires(MusaePermissoes::admin)
                .then(Commands.literal("jogador")
                        .then(Commands.argument("jogador", GameProfileArgument.gameProfile())
                                .then(Commands.literal("historico")
                                        .executes(ctx -> historico(ctx, 1))
                                        .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                                .executes(ctx -> historico(ctx, IntegerArgumentType.getInteger(ctx, "pagina")))))
                                .then(Commands.literal("resumo")
                                        .executes(MonitoramentoCommand::resumo))))
                .then(Commands.literal("horario")
                        .then(Commands.literal("status")
                                .then(Commands.literal("ativar").executes(ctx -> alternarHorario(ctx, true)))
                                .then(Commands.literal("desativar").executes(ctx -> alternarHorario(ctx, false)))
                                .then(Commands.literal("verificar").executes(MonitoramentoCommand::statusHorario)))
                        .then(Commands.literal("definir")
                                .then(Commands.argument("horario_inicio horario_fim", StringArgumentType.greedyString())
                                        .executes(MonitoramentoCommand::definirHorario))));
    }

    private static String extrairNome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<GameProfile> perfis = GameProfileArgument.getGameProfiles(ctx, "jogador");
        return perfis.iterator().next().getName();
    }

    private static int historico(CommandContext<CommandSourceStack> ctx, int pagina) throws CommandSyntaxException {
        String nome = extrairNome(ctx);
        HistoricoJogador dados = HistoricoLoginManager.carregar(ctx.getSource().getServer(), nome);

        if (dados == null || dados.sessoes.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Nenhum histórico de login encontrado para " + nome + ".")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        List<SessaoJogador> sessoes = dados.sessoes;
        int total = sessoes.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) ITENS_POR_PAGINA));
        pagina = Math.min(pagina, totalPaginas);

        long media = HistoricoLoginManager.mediaPorSessao(dados);

        ctx.getSource().sendSuccess(() -> Component.literal("== Histórico de login de " + nome + " ==")
                .withStyle(ChatFormatting.GOLD), false);
        ctx.getSource().sendSuccess(() -> Component.literal("Total de logins: " + total
                + " | Média por sessão: " + FormatoUtil.formatarDuracao(media)).withStyle(ChatFormatting.GRAY), false);

        int inicio = Math.max(0, total - (pagina * ITENS_POR_PAGINA));
        int fim = total - ((pagina - 1) * ITENS_POR_PAGINA);

        int linhaAtual = 0;
        for (int i = fim - 1; i >= inicio; i--) {
            SessaoJogador sessao = sessoes.get(i);
            String entradaStr = FormatoUtil.formatarDataHora(sessao.entrada);
            ChatFormatting corLinha = (linhaAtual % 2 == 0) ? COR_PAR : COR_IMPAR;
            linhaAtual++;

            if (sessao.saida != null) {
                String saidaStr = FormatoUtil.formatarDataHora(sessao.saida);
                ctx.getSource().sendSuccess(() -> Component.literal("Entrada: " + entradaStr + " -> Saída: " + saidaStr)
                        .withStyle(corLinha), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("Entrada: " + entradaStr + " -> Saída: ainda online")
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }

        int paginaFinal = pagina;
        int totalPaginasFinal = totalPaginas;
        ctx.getSource().sendSuccess(() -> Component.literal("Página " + paginaFinal + " de " + totalPaginasFinal)
                .withStyle(ChatFormatting.DARK_GRAY), false);

        return 1;
    }

    private static int resumo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String nome = extrairNome(ctx);
        HistoricoJogador dados = HistoricoLoginManager.carregar(ctx.getSource().getServer(), nome);

        if (dados == null || dados.sessoes.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Nenhum histórico de login encontrado para " + nome + ".")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        SessaoJogador ultima = HistoricoLoginManager.ultimaSessao(dados);
        long tempoTotal = HistoricoLoginManager.tempoTotalJogado(dados);
        long media = HistoricoLoginManager.mediaPorSessao(dados);
        int totalLogins = dados.sessoes.size();

        ctx.getSource().sendSuccess(() -> Component.literal("== Resumo de " + nome + " ==")
                .withStyle(ChatFormatting.GOLD), false);

        List<String> linhas = List.of(
                "Último login: " + FormatoUtil.formatarDataHora(ultima.entrada),
                "Endereço IP: " + ultima.ip,
                "Localização: " + String.format("%.0f, %.0f, %.0f", ultima.x, ultima.y, ultima.z) + " (" + ultima.dimensao + ")",
                "Tempo total jogado: " + FormatoUtil.formatarDuracao(tempoTotal),
                "Quantidade de logins: " + totalLogins,
                "Média de tempo por sessão: " + FormatoUtil.formatarDuracao(media)
        );

        for (int i = 0; i < linhas.size(); i++) {
            String texto = linhas.get(i);
            ChatFormatting corLinha = (i % 2 == 0) ? COR_PAR : COR_IMPAR;
            ctx.getSource().sendSuccess(() -> Component.literal(texto).withStyle(corLinha), false);
        }

        return 1;
    }

    private static int alternarHorario(CommandContext<CommandSourceStack> ctx, boolean ativo) {
        MinecraftServer servidor = ctx.getSource().getServer();
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(servidor);
        config.ativado = ativo;
        ConfiguracaoHorarioProibido.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.literal(ativo
                ? "Monitoramento de horário proibido foi ativado."
                : "Monitoramento de horário proibido foi desativado."
        ).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int statusHorario(CommandContext<CommandSourceStack> ctx) {
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(ctx.getSource().getServer());
        String status = config.ativado ? "ativado" : "desativado";

        ctx.getSource().sendSuccess(() -> Component.literal("Horário proibido: " + status
                + " | Das " + config.horaInicio + " às " + config.horaFim + " (horário de Brasília)"
        ).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    private static int definirHorario(CommandContext<CommandSourceStack> ctx) {
        String bruto = StringArgumentType.getString(ctx, "horario_inicio horario_fim").trim();
        String[] partes = bruto.split("\\s+");

        if (partes.length != 2) {
            ctx.getSource().sendFailure(Component.literal(
                    "Uso: /musae monitoramento horario definir <inicio> <fim> (exemplo: 22:00 06:00)")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        String inicio = partes[0];
        String fim = partes[1];

        try {
            LocalTime.parse(inicio);
            LocalTime.parse(fim);
        } catch (DateTimeParseException e) {
            ctx.getSource().sendFailure(Component.literal("Formato de horário inválido. Use HH:mm, exemplo: 22:00.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        MinecraftServer servidor = ctx.getSource().getServer();
        ConfiguracaoHorarioProibido config = ConfiguracaoHorarioProibido.get(servidor);
        config.horaInicio = inicio;
        config.horaFim = fim;
        ConfiguracaoHorarioProibido.salvar(servidor);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Horário proibido definido: " + inicio + " às " + fim + " (horário de Brasília).")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
}
