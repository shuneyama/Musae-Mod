package net.shune.musae.comandos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;

public final class MusaeCommands {
    private MusaeCommands() {
    }

    private static LiteralArgumentBuilder<CommandSourceStack> lit(String nome) {
        return Commands.literal(nome);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> nomeArea() {
        return Commands.argument("nome", StringArgumentType.string())
                .suggests(Canon::sugerirAreas);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> nomeNota() {
        return Commands.argument("nome", StringArgumentType.string())
                .suggests(Notas::sugerir);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> textoLivre(String nome) {
        return Commands.argument(nome, StringArgumentType.greedyString());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> eqNode(String equipe) {
        return lit(equipe)
                .requires(s -> Teams.podeMensagem(s, equipe))
                .then(lit("adicionar")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(ctx -> Teams.adicionar(ctx, equipe))))
                .then(lit("remover")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(ctx -> Teams.remover(ctx, equipe))))
                .then(lit("ver").requires(s -> s.hasPermission(2)).executes(ctx -> Teams.ver(ctx, equipe)))
                .then(lit("online").requires(s -> s.hasPermission(2)).executes(ctx -> Teams.online(ctx, equipe)))
                .then(lit("mensagem")
                        .executes(ctx -> Teams.chatVer(ctx, equipe))
                        .then(Commands.argument("texto", StringArgumentType.string())
                                .executes(ctx -> Teams.chatEnviar(ctx, equipe))));
    }

    public static void anexar(LiteralArgumentBuilder<CommandSourceStack> raiz) {

        LiteralArgumentBuilder<CommandSourceStack> equipe = lit("equipe");
        for (String nome : Teams.NOMES) {
            equipe = equipe.then(eqNode(nome));
        }
        equipe = equipe.then(lit("mensagem")
                .executes(Teams::chatVer)
                .then(Commands.argument("texto", StringArgumentType.string())
                        .executes(Teams::chatEnviar)));

        LiteralArgumentBuilder<CommandSourceStack> temp = lit("musae")

                .then(lit("vidas_canon")
                        .requires(s -> s.hasPermission(2))

                        .then(lit("area")
                                .then(lit("pos1").executes(ctx -> Canon.setPos(ctx, 1)))
                                .then(lit("pos2").executes(ctx -> Canon.setPos(ctx, 2)))
                                .then(lit("adicionar")
                                        .then(Commands.argument("nome", StringArgumentType.string())
                                                .executes(Canon::areaAdicionar)))
                                .then(lit("ver").executes(Canon::areaVer))
                                .then(lit("mostrar")
                                        .then(nomeArea().executes(Canon::areaMostrar)))
                                .then(lit("remover")
                                        .then(nomeArea().executes(Canon::areaRemover))))

                        .then(lit("teleportar")
                                .then(nomeArea().executes(Canon::teleportar)))

                        .then(Commands.argument("alvos", EntityArgument.players())
                                .then(lit("ativar").executes(Canon::ativar))
                                .then(lit("desativar").executes(Canon::desativar))
                                .then(lit("ver").executes(Canon::ver))
                                .then(lit("adicionar").executes(ctx -> Canon.mudarVida(ctx, 1)))
                                .then(lit("remover").executes(ctx -> Canon.mudarVida(ctx, -1)))
                                .then(lit("resetar").executes(Canon::resetar))))

                .then(lit("bloco_notas")
                        .requires(Teams::permitido)
                        .then(lit("adicionar")
                                .then(Commands.argument("nome", StringArgumentType.string())
                                        .then(textoLivre("texto").executes(Notas::adicionar))))
                        .then(lit("ver").executes(Notas::ver))
                        .then(lit("buscar")
                                .then(Commands.argument("busca", StringArgumentType.string())
                                        .executes(Notas::buscar)))
                        .then(lit("abrir")
                                .then(nomeNota().executes(Notas::abrir)))
                        .then(lit("editar")
                                .then(nomeNota()
                                        .then(textoLivre("texto").executes(ctx -> Notas.atualizar(ctx, false)))))
                        .then(lit("anexar")
                                .then(nomeNota()
                                        .then(textoLivre("texto").executes(ctx -> Notas.atualizar(ctx, true)))))
                        .then(lit("remover")
                                .then(nomeNota().executes(Notas::remover))))

                .then(equipe)

                .then(lit("ajuda")
                        .executes(Help::ajuda)
                        .then(lit("aceitar")
                                .then(Commands.argument("id", IntegerArgumentType.integer())
                                        .executes(Help::aceitar)))
                        .then(lit("recusar")
                                .then(Commands.argument("id", IntegerArgumentType.integer())
                                        .executes(Help::recusar)))
                        .then(lit("teleportar")
                                .then(Commands.argument("id", IntegerArgumentType.integer())
                                        .executes(Help::ir)))
                        .then(lit("historico")
                                .requires(Teams::permitido)
                                .executes(Help::historico))
                        .then(textoLivre("motivo").executes(Help::ajudaComMotivo)))

                .then(lit("warn")
                        .then(lit("adicionar")
                                .requires(Teams::permitido)
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .then(textoLivre("motivo").executes(Warns::adicionar))))
                        .then(lit("ver")
                                .requires(Teams::permitido)
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(Warns::ver)))
                        .then(lit("remover")
                                .requires(Teams::permitido)
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .then(Commands.argument("numero", IntegerArgumentType.integer())
                                                .executes(Warns::remover))))
                        .then(lit("limpar")
                                .requires(s -> s.hasPermission(2))
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                        .executes(Warns::limpar)))
                        .then(lit("lista")
                                .requires(Teams::permitido)
                                .executes(Warns::listaGeral))
                        .then(lit("meus").executes(Warns::meus)))

                .then(lit("log")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> Logs.ver(ctx, 1))
                        .then(lit("resetar").executes(Logs::resetar))
                        .then(Commands.argument("pagina", IntegerArgumentType.integer(1))
                                .executes(ctx -> Logs.ver(ctx, IntegerArgumentType.getInteger(ctx, "pagina")))))

                .then(lit("evento")
                        .requires(s -> s.hasPermission(2))
                        .then(lit("pos1").executes(ctx -> Evento.setPos(ctx, 1)))
                        .then(lit("pos2").executes(ctx -> Evento.setPos(ctx, 2)))
                        .then(lit("adicionar")
                                .then(lit("nevoa").executes(ctx -> Evento.adicionar(ctx, "nevoa")))
                                .then(lit("cegueira").executes(ctx -> Evento.adicionar(ctx, "cegueira"))))
                        .then(lit("remover")
                                .then(lit("nevoa").executes(ctx -> Evento.removerEfeito(ctx, "nevoa")))
                                .then(lit("cegueira").executes(ctx -> Evento.removerEfeito(ctx, "cegueira"))))
                        .then(lit("ativar").executes(Evento::ativar))
                        .then(lit("desativar").executes(Evento::desativar))
                        .then(lit("ver").executes(Evento::ver)));

        for (com.mojang.brigadier.tree.CommandNode<CommandSourceStack> no : temp.getArguments()) {
            raiz.then(no);
        }
    }
}
