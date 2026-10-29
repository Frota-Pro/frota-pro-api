package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UnificarRotasResponse {

    /** true = só mostrou o que seria feito, nada foi alterado. */
    private boolean simulacao;
    private int rotasRemovidas;
    private int cargasMovidas;
    private List<Grupo> grupos;

    @Getter
    @Builder
    public static class Grupo {
        private String codigoMantida;
        private String nomeAtual;
        /** Nome que a rota passa a ter (sem o sufixo ignorado). */
        private String nomeFinal;
        private List<Repetida> repetidas;
    }

    @Getter
    @Builder
    public static class Repetida {
        private String codigo;
        private String nome;
        private long quantidadeCargas;
    }
}
