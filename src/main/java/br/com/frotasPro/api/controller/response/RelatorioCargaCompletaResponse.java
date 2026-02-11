package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCargaCompletaResponse {
    private String numeroCarga;
    private String codigoCarga;
    private String statusCarga;

    private String motorista;
    private String caminhao;
    private String rota;

    private LocalDate dataSaida;
    private LocalDate dataChegada;

    private BigDecimal valorTotal;
    private BigDecimal pesoCarga;

    private String observacaoMotorista;

    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private String tipo; // NOTA / PARADA
        private LocalDate data;
        private String descricao;
        private String cidade;
        private BigDecimal valor;
        private BigDecimal km;
    }
}
