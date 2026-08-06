package br.com.frotasPro.api.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Fuso horário de referência para cálculos de "data/hora atual" que alimentam
 * a sincronização com o WinThor (ex.: {@code LocalDate.now()} usado como
 * dataInicial/dataFinal do período de cargas faturadas).
 * <p>
 * Sem isso, {@code LocalDate.now()}/{@code LocalDateTime.now()} usam o fuso
 * do container (UTC por padrão nas imagens desta aplicação), o que faz o
 * "hoje" virar o dia seguinte a partir das 21h no horário do Brasil,
 * fazendo a sincronização automática buscar cargas do dia errado no WinThor.
 */
public final class FusoHorarioUtils {

    public static final ZoneId ZONE_BRASIL = ZoneId.of("America/Recife");

    private FusoHorarioUtils() {
    }

    public static LocalDate hojeBrasil() {
        return LocalDate.now(ZONE_BRASIL);
    }

    public static LocalDateTime agoraBrasil() {
        return LocalDateTime.now(ZONE_BRASIL);
    }
}
