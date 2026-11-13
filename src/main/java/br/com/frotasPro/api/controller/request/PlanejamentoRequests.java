package br.com.frotasPro.api.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/** Corpos das chamadas do planejamento de cargas. */
public final class PlanejamentoRequests {

    private PlanejamentoRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CriarPlanilha {
        /** Dia da planilha; se vazio, hoje. */
        private LocalDate data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DefinirComplemento {
        /** Número do carregamento principal; vazio desfaz o complemento. */
        private Integer principal;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DefinirRota {
        /** Códigos do cadastro (ex.: MOT-000001, AJU-000003, CAM-000017). Vazios limpam a escolha; ajudantes podem ser vários. */
        private String motoristaCodigo;
        private List<String> ajudantesCodigos;
        private String caminhaoCodigo;
    }

    /** Box e saída da rota. Vazios limpam. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class DefinirSaida {
        private Integer boxNumero;
        /** HOJE ou AMANHA. */
        private String diaSaida;
        /** MADRUGADA, MANHA, TARDE ou NOITE. */
        private String turnoSaida;
    }

    /** Vários carregamentos de uma vez (tirar, separar). */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Lote {
        private List<Integer> numeros;
    }

    /** Vários carregamentos viram complemento de um principal. */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Juntar {
        private Integer principal;
        private List<Integer> numeros;
    }
}
