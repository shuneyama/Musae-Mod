package net.shune.musae.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.shune.musae.network.BalaoPacket;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.nickname.NicknameData;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {

    public static void enviarMensagemLocal(ServerPlayer remetente, String mensagem) {
        if (!AntiSpamManager.podeEnviar(remetente.getUUID(), mensagem)) {
            remetente.sendSystemMessage(Component.literal("Aguarde antes de enviar a mesma mensagem!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        double raio = ConfiguracaoChat.get(remetente.getServer()).raioLocal;
        List<ServerPlayer> receptores = getJogadoresNoAlcance(remetente, raio);

        MutableComponent formatada = Component.literal("§7[§aLocal§7] ")
                .append(getNomeEstilizado(remetente))
                .append(Component.literal("§r: " + mensagem));

        for (ServerPlayer receptor : receptores) {
            receptor.sendSystemMessage(formatada);
        }

        if (ConfiguracaoBalao.isAtivo(remetente.getServer(), remetente.getUUID())) {
            PlayerBalaoData dadosBalao = PlayerBalaoData.get(remetente.getServer(), remetente.getUUID());

            BalaoDebug.log("Enviando balão: remetente=" + remetente.getGameProfile().getName()
                    + " mensagem=\"" + mensagem + "\" receptoresNoAlcance=" + receptores.size());

            NetworkHandler.sendToAllTracking(remetente, new BalaoPacket(
                    remetente.getUUID(), mensagem,
                    dadosBalao.corTexto, dadosBalao.corFundo, dadosBalao.corBorda,
                    dadosBalao.altura, dadosBalao.estilo));
        } else {
            BalaoDebug.log("Balão desativado para " + remetente.getGameProfile().getName() + ", pacote não enviado.");
        }
    }

    public static void enviarMensagemGlobal(ServerPlayer remetente, String mensagem) {
        if (!AntiSpamManager.podeEnviar(remetente.getUUID(), mensagem)) {
            remetente.sendSystemMessage(Component.literal("Aguarde antes de enviar a mesma mensagem!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        List<ServerPlayer> receptores = new ArrayList<>(remetente.getServer().getPlayerList().getPlayers());

        MutableComponent formatada = Component.literal("§7[§eGlobal§7] ")
                .append(getNomeEstilizado(remetente))
                .append(Component.literal("§r: " + mensagem));

        for (ServerPlayer receptor : receptores) {
            receptor.sendSystemMessage(formatada);
        }
    }

    private static MutableComponent getNomeEstilizado(ServerPlayer player) {
        if (player.getServer() != null) {
            NicknameData data = NicknameData.get(player.getServer());
            MutableComponent estilizado = data.getStyledNickname(player.getUUID());
            if (estilizado != null) return estilizado;
        }
        return Component.literal(player.getScoreboardName());
    }

    public static List<ServerPlayer> getJogadoresNoAlcance(ServerPlayer remetente, double distancia) {
        List<ServerPlayer> resultado = new ArrayList<>();

        for (ServerPlayer player : remetente.serverLevel().players()) {
            if (player.distanceTo(remetente) <= distancia) {
                resultado.add(player);
            }
        }

        return resultado;
    }
}
