package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Uma planilha de planejamento inteira: as rotas (principal + complementos) com totais, motorista, ajudante e caminhão. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanejamentoPlanilhaResponse {

    private UUID id;
    private LocalDate data;
    /** ABERTA ou FECHADA. */
    private String status;
    private String criadaPor;
    private LocalDateTime criadaEm;
    private String fechadaPor;
    private LocalDateTime fechadaEm;
    /** Fechada no mesmo dia dela: dá para reabrir. */
    private boolean podeReabrir;

    private int totalCarregamentos;
    private int totalRotas;
    /** Soma de todos os carregamentos, em TONELADAS. */
    private BigDecimal pesoToneladas;
    /** Soma do valor dos pedidos de todos os carregamentos, em reais. */
    private BigDecimal valorTotal;

    private List<Rota> rotas;
    /** Os boxes e quem está ocupando cada um (rota ainda não faturada). */
    private List<Box> boxes;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rota {
        /** Número do carregamento principal. */
        private Integer numeroPrincipal;
        private Pessoa motorista;
        private List<Pessoa> ajudantes;
        private Box box;
        /** HOJE ou AMANHA; null se ainda não escolheu. */
        private String diaSaida;
        /** MADRUGADA, MANHA, TARDE ou NOITE; null se ainda não escolheu. */
        private String turnoSaida;
        private Veiculo caminhao;
        /** Cor da rota (índice da paleta da tela); só existe quando a rota tem complementos. */
        private Integer cor;
        private BigDecimal pesoToneladas;
        private BigDecimal valorTotal;
        /** Clientes DISTINTOS entre o principal e os complementos. */
        private int totalClientes;
        private int totalPedidos;
        /** Destinos distintos da rota, na ordem em que aparecem (principal primeiro). */
        private List<String> destinos;
        /** Quantidade de cidades atendidas (destinos distintos). */
        private int totalCidades;
        /** As mesmas informações de {@link #conflitos}, em frases prontas para mostrar. */
        private List<String> avisos;
        /** Motorista, ajudante ou caminhão desta rota que também está em outra rota na mesma hora. */
        private List<Conflito> conflitos;
        /** O principal primeiro, depois os complementos. */
        private List<Carregamento> carregamentos;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Carregamento {
        private Integer numero;
        private boolean principal;
        private String destino;
        private BigDecimal pesoToneladas;
        private BigDecimal valorTotal;
        private int totalClientes;
        /** Códigos dos clientes: a tela soma clientes distintos de qualquer seleção sem repetir. */
        private List<Integer> codigosClientes;
        private int totalPedidos;
        private LocalDate dataMontagem;
        /** Motorista e veículo que o WinThor gravou no carregamento (ex.: 10 e 2 = cliente vem buscar). */
        private Integer codMotoristaWinThor;
        private Integer codVeiculoWinThor;
        /** Nome do motorista e placa do veículo que o WinThor gravou no carregamento. */
        private String nomeMotoristaWinThor;
        private String placaVeiculoWinThor;
        /** Já tem nota no WinThor: a planilha não mexe mais (só dá para tirar). */
        private boolean faturada;
        /** Status da carga no FrotaPRO (SINCRONIZADA, EM_ROTA, FINALIZADA...), quando ela já existe. */
        private String statusCarga;
        /** Motorista e caminhão que a carga realmente tem no FrotaPRO (para comparar com o planejado). */
        private String motoristaCarga;
        private String caminhaoCarga;
    }

    /** Uma inconsistência: quem (ou o quê) aparece em duas rotas que podem sair ao mesmo tempo. */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Conflito {
        /** MOTORISTA, AJUDANTE ou CAMINHAO. */
        private String tipo;
        private String codigo;
        /** Nome do motorista/ajudante ou placa do caminhão. */
        private String nome;
        /** As outras rotas em que aparece. */
        private List<Integer> rotas;
        /** true quando as duas rotas têm o MESMO dia e turno; false quando algum dos horários ainda não foi definido. */
        private boolean mesmoHorario;
        private String texto;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Box {
        private Integer numero;
        private String nome;
        /** Número do principal da rota que está usando o box (ainda não faturada); null se livre. */
        private Integer ocupadoPorRota;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pessoa {
        private UUID id;
        private String codigo;
        private String nome;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Veiculo {
        private UUID id;
        private String codigo;
        private String placa;
        private String descricao;
    }
}
