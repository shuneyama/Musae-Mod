package net.shune.musae.monitor;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ConfiguracaoHorarioProibido {

    public static final ZoneId FUSO_BRASILIA = ZoneId.of("America/Sao_Paulo");

    public boolean ativado = false;
    public String horaInicio = "22:00";
    public String horaFim = "06:00";

    private static ConfiguracaoHorarioProibido instancia;

    private static Path arquivo(MinecraftServer servidor) {
        return ArquivoUtil.pastaDoMundo(servidor).resolve("horario_proibido_config.json");
    }

    public static ConfiguracaoHorarioProibido get(MinecraftServer servidor) {
        if (instancia == null) {
            instancia = ArquivoUtil.lerJson(arquivo(servidor), ConfiguracaoHorarioProibido.class, new ConfiguracaoHorarioProibido());
        }
        return instancia;
    }

    public static void salvar(MinecraftServer servidor) {
        if (instancia != null) {
            ArquivoUtil.escreverJson(arquivo(servidor), instancia);
        }
    }

    public static void resetarCache() {
        instancia = null;
    }

    public boolean dentroDoHorario() {
        if (!ativado) return false;

        LocalTime agora = ZonedDateTime.now(FUSO_BRASILIA).toLocalTime();
        LocalTime inicio = LocalTime.parse(horaInicio);
        LocalTime fim = LocalTime.parse(horaFim);

        if (inicio.equals(fim)) return true;

        if (inicio.isBefore(fim)) {
            return !agora.isBefore(inicio) && agora.isBefore(fim);
        }

        return !agora.isBefore(inicio) || agora.isBefore(fim);
    }
}
