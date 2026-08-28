package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.controller.response.DashboardVisaoGeralResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.DocumentoCaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.MultaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.service.pneu.PneuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Resumo operacional extra pra tela inicial do Dashboard: alertas de
 * vencimento, vida útil de pneu, status da frota, consumo médio, OS e multas
 * em aberto, e cargas por status. Cada pedaço reaproveita consultas/regras
 * que já existiam espalhadas pelo sistema (notificações, Analytics,
 * relatórios de manutenção) — aqui só agrega tudo numa chamada só.
 */
@Service
@RequiredArgsConstructor
public class BuscarDashboardVisaoGeralService {

    private static final List<StatusManutencao> STATUS_EM_ABERTO = List.of(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO);

    private final MotoristaRepository motoristaRepository;
    private final DocumentoCaminhaoRepository documentoCaminhaoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final MultaRepository multaRepository;
    private final PneuService pneuService;
    private final ParametroSistemaService parametroSistemaService;

    @Transactional(readOnly = true)
    public DashboardVisaoGeralResponse executar() {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();

        return DashboardVisaoGeralResponse.builder()
                .alertas(alertas(hoje))
                .pneus(pneus())
                .frota(frota())
                .consumoMedioKmLMes(consumoMedioKmLMes(hoje))
                .manutencoes(manutencoes(hoje))
                .multas(multas(hoje))
                .cargasPorStatus(cargasPorStatus(hoje))
                .build();
    }

    private DashboardVisaoGeralResponse.AlertasResumo alertas(LocalDate hoje) {
        LocalDate limite = hoje.plusDays(parametroSistemaService.buscarOuPadrao().getDiasAntecedenciaVencimentoDocumento());

        return DashboardVisaoGeralResponse.AlertasResumo.builder()
                .cnhVencendo(motoristaRepository.countCnhVencendoOuVencida(limite))
                .documentosCaminhaoVencendo(documentoCaminhaoRepository.countVencendoOuVencido(limite))
                .build();
    }

    private DashboardVisaoGeralResponse.PneusResumo pneus() {
        long[] contagem = pneuService.contarAlertasVidaUtil();
        return DashboardVisaoGeralResponse.PneusResumo.builder()
                .vencidos(contagem[0])
                .proximoFim(contagem[1])
                .ok(contagem[2])
                .build();
    }

    private DashboardVisaoGeralResponse.FrotaResumo frota() {
        return DashboardVisaoGeralResponse.FrotaResumo.builder()
                .disponiveis(caminhaoRepository.countByStatusAndAtivoTrue(Status.DISPONIVEL))
                .emRota(caminhaoRepository.countByStatusAndAtivoTrue(Status.EM_ROTA))
                .emManutencao(manutencaoRepository.countCaminhoesDistintosComManutencaoEmAberto(STATUS_EM_ABERTO))
                .totalAtivos(caminhaoRepository.countByAtivoTrue())
                .build();
    }

    /** Mesma fórmula usada no Analytics (comparação "frota toda"): km rodado nas cargas finalizadas no mês / litros abastecidos no mês. */
    private BigDecimal consumoMedioKmLMes(LocalDate hoje) {
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDateTime inicioMesDt = inicioMes.atStartOfDay();
        LocalDateTime fimDt = FusoHorarioUtils.agoraBrasil();

        List<Carga> cargasFinalizadas = cargaRepository.findByStatusCargaAndDtChegadaBetween(Status.FINALIZADA, inicioMes, hoje);
        long kmRodado = cargasFinalizadas.stream().mapToLong(c -> nvl(c.calcularKmTotal())).sum();

        List<Abastecimento> abastecimentos = abastecimentoRepository.findAllByDtAbastecimentoBetween(inicioMesDt, fimDt);
        BigDecimal totalLitros = abastecimentos.stream()
                .map(Abastecimento::getQtLitros)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalLitros.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(kmRodado).divide(totalLitros, 2, RoundingMode.HALF_UP);
    }

    private DashboardVisaoGeralResponse.ManutencoesResumo manutencoes(LocalDate hoje) {
        int diasManutencaoEstagnada = parametroSistemaService.buscarOuPadrao().getDiasManutencaoEstagnada();
        LocalDate limite = hoje.minusDays(diasManutencaoEstagnada);

        return DashboardVisaoGeralResponse.ManutencoesResumo.builder()
                .abertas(manutencaoRepository.countByStatusManutencaoIn(STATUS_EM_ABERTO))
                .atrasadas(manutencaoRepository.countEstagnadas(STATUS_EM_ABERTO, limite))
                .build();
    }

    private DashboardVisaoGeralResponse.MultasResumo multas(LocalDate hoje) {
        LocalDate minVencimento = multaRepository.minVencimentoPagamento(StatusPagamentoMulta.PENDENTE, hoje);
        LocalDate minRecurso = multaRepository.minLimiteRecurso(StatusPagamentoMulta.PENDENTE, hoje);
        LocalDate prazoMaisProximo = menorData(minVencimento, minRecurso);

        return DashboardVisaoGeralResponse.MultasResumo.builder()
                .pendentes(multaRepository.countByStatusPagamento(StatusPagamentoMulta.PENDENTE))
                .valorTotal(multaRepository.sumValorByStatusPagamento(StatusPagamentoMulta.PENDENTE))
                .prazoMaisProximo(prazoMaisProximo)
                .build();
    }

    /** Cargas CRIADAS no mês corrente, agrupadas pelo status atual — não o histórico todo da empresa. */
    private List<DashboardVisaoGeralResponse.CargaStatusResumo> cargasPorStatus(LocalDate hoje) {
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = FusoHorarioUtils.agoraBrasil();

        return List.of(
                cargaStatusResumo(Status.EM_ROTA, "Em rota", inicioMes, fim),
                cargaStatusResumo(Status.SINCRONIZADA, "Sincronizada", inicioMes, fim),
                cargaStatusResumo(Status.FINALIZADA, "Finalizada", inicioMes, fim)
        );
    }

    private DashboardVisaoGeralResponse.CargaStatusResumo cargaStatusResumo(Status status, String label, LocalDateTime inicioMes, LocalDateTime fim) {
        return DashboardVisaoGeralResponse.CargaStatusResumo.builder()
                .status(status.name())
                .statusLabel(label)
                .total(cargaRepository.countByStatusCargaAndCriadoEmBetween(status, inicioMes, fim))
                .build();
    }

    private LocalDate menorData(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    private long nvl(Integer v) {
        return v == null ? 0L : v;
    }
}
