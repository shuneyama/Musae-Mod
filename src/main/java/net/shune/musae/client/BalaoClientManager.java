package net.shune.musae.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.chat.BalaoConfig;
import net.shune.musae.chat.BalaoDebug;
import net.shune.musae.chat.BalaoStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class BalaoClientManager {

    private static final Map<UUID, List<BalaoMensagem>> BALOES_POR_JOGADOR = new ConcurrentHashMap<>();

    public static void criarBalao(UUID jogadorUUID, String texto, int duracaoTicks,
                                   int corTexto, int corFundo, int corBorda, float altura, BalaoStyle estilo) {
        List<BalaoMensagem> lista = BALOES_POR_JOGADOR.computeIfAbsent(jogadorUUID, id -> new ArrayList<>());

        int[] delays = calcularDelaysDigitacao(texto);
        BalaoMensagem msg = new BalaoMensagem(texto, corTexto, corFundo, corBorda, altura, estilo,
                System.currentTimeMillis(), delays, duracaoTicks * 50L);

        lista.add(0, msg);
        while (lista.size() > BalaoConfig.MAX_BALLOONS_PER_PLAYER) {
            lista.remove(lista.size() - 1);
        }

        BalaoDebug.log("Balão criado para " + jogadorUUID + " — total de balões ativos agora: " + lista.size());
    }

    private static int[] calcularDelaysDigitacao(String texto) {
        int[] delays = new int[texto.length()];
        for (int i = 0; i < texto.length(); i++) {
            if (i > 0) {
                char anterior = texto.charAt(i - 1);
                delays[i] = BalaoConfig.PUNCTUATION_CHARS.indexOf(anterior) != -1
                        ? BalaoConfig.PUNCTUATION_DELAY_MS
                        : BalaoConfig.CHAR_DELAY_MS;
            } else {
                delays[i] = BalaoConfig.CHAR_DELAY_MS;
            }
        }
        return delays;
    }

    public static List<BalaoMensagem> getBaloes(UUID jogadorUUID) {
        List<BalaoMensagem> lista = BALOES_POR_JOGADOR.get(jogadorUUID);
        if (lista == null || lista.isEmpty()) return List.of();

        long agora = System.currentTimeMillis();
        lista.removeIf(msg -> agora - msg.startTime() >= msg.tempoTotalDigitacao() + msg.duracaoExibicaoMs());

        return lista;
    }

    public record BalaoMensagem(
            String textoCompleto,
            int corTexto,
            int corFundo,
            int corBorda,
            float altura,
            BalaoStyle estilo,
            long startTime,
            int[] delaysDigitacao,
            long duracaoExibicaoMs
    ) {
        public String getTextoVisivel(long agora) {
            long decorrido = agora - startTime;
            int caracteresVisiveis = 0;
            long delayAcumulado = 0;

            for (int i = 0; i < delaysDigitacao.length; i++) {
                delayAcumulado += delaysDigitacao[i];
                if (decorrido >= delayAcumulado) {
                    caracteresVisiveis = i + 1;
                } else {
                    break;
                }
            }

            return textoCompleto.substring(0, Math.min(caracteresVisiveis, textoCompleto.length()));
        }

        public long tempoTotalDigitacao() {
            long total = 0;
            for (int delay : delaysDigitacao) total += delay;
            return total;
        }
    }
}
