package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Lista da conferência "Reentrega: motorista/caminhão diferente do manifesto". */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenciaMotoristaCaminhaoResponse {

    private LocalDate inicio;
    private LocalDate fim;
    private Long total;
    private List<Linha> linhas;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private String numeroCarga;
        private String numeroCargaExterno;
        private String statusCarga;
        /** true se a carga já passou da fase SINCRONIZADA (iniciada ou finalizada): confira com mais cuidado. */
        private boolean iniciada;
        private LocalDate dtSaida;
        private Integer kmInicial;
        private BigDecimal pesoCarga;
        private BigDecimal valorTotal;

        /** O que o MDF-e (usado pela sincronização) diz. */
        private Manifesto manifesto;
        /** O que o carregamento no WinThor diz. */
        private Pessoa carregamento;
        /** O que o FrotaPRO tem hoje na carga. */
        private Pessoa atualNoFrota;
        private boolean motoristaDefinidoManualmente;
        private boolean caminhaoDefinidoManualmente;

        /** O que "Confirmar" vai fazer. */
        private boolean trocarMotorista;
        private String codigoMotoristaNovo;
        private String nomeMotoristaNovo;
        private boolean trocarCaminhao;
        private String codigoCaminhaoNovo;
        private String placaCaminhaoNovo;

        private boolean podeConfirmar;
        private String aviso;

        /** Resumo da proposta do WinThor, para o "ignorar"; uso interno, não vai para a tela. */
        @com.fasterxml.jackson.annotation.JsonIgnore
        private String assinaturaParaDecisao;

        private List<Transferencia> transferencias;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Manifesto {
        private Long numMdfe;
        private String situacao;
        private LocalDateTime dataGeracao;
        private String codigoMotorista;
        private String nomeMotorista;
        private String codigoVeiculo;
        private String placa;
    }

    /** Motorista e veículo, com o código de onde vieram (WinThor ou FrotaPRO). */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pessoa {
        private String codigoMotorista;
        private String nomeMotorista;
        private String codigoVeiculo;
        private String placa;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transferencia {
        private Long numNota;
        private Integer numCarAnterior;
        private Integer numCarAtual;
        private LocalDateTime dataTransferencia;
        private String motivo;
    }
}
