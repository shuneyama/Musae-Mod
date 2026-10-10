package net.shune.musae.monitor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FormatoUtil {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String formatarDataHora(long epochMillis) {
        ZonedDateTime data = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ConfiguracaoHorarioProibido.FUSO_BRASILIA);
        return data.format(FORMATO_DATA_HORA);
    }

    public static String formatarDuracao(long millis) {
        long segundosTotais = millis / 1000;
        long dias = segundosTotais / 86400;
        long horas = (segundosTotais % 86400) / 3600;
        long minutos = (segundosTotais % 3600) / 60;

        StringBuilder resultado = new StringBuilder();
        if (dias > 0) resultado.append(dias).append("d ");
        if (horas > 0 || dias > 0) resultado.append(horas).append("h ");
        resultado.append(minutos).append("min");

        return resultado.toString();
    }
}
