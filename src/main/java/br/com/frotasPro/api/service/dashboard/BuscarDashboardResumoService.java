package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.controller.response.DashboardCargaRecenteResponse;
import br.com.frotasPro.api.controller.response.DashboardResumoResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarDashboardResumoService {

    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final MetaRepository metaRepository;
    private final ManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public DashboardResumoResponse executar() {

        LocalDate hoje = LocalDate.now();

        long cargasAtivas = cargaRepository.countByStatusCarga(Status.EM_ROTA);
        long finalizadasHoje = cargaRepository.countByStatusCargaAndDtChegada(Status.FINALIZADA, hoje);

        LocalDateTime inicio30d = LocalDateTime.now().minusDays(30);
        BigDecimal litros30d = abastecimentoRepository.sumLitrosFrom(inicio30d);
        if (litros30d == null) litros30d = BigDecimal.ZERO;

        long metasAtivas = metaRepository.countMetasAtivas(StatusMeta.EM_ANDAMENTO, hoje);

        long osAbertas = manutencaoRepository.countByStatusManutencaoIn(
                List.of(StatusManutencao.AGENDADA, StatusManutencao.EM_ANDAMENTO)
        );

        List<Carga> recentes = cargaRepository.findTop5ByOrderByCriadoEmDesc();

        List<DashboardCargaRecenteResponse> cargasRecentes = recentes.stream()
                .map(this::toCargaRecente)
                .toList();

        return DashboardResumoResponse.builder()
                .cargasAtivas(cargasAtivas)
                .finalizadasHoje(finalizadasHoje)
                .litros30d(litros30d)
                .metasAtivas(metasAtivas)
                .osAbertas(osAbertas)
                .cargasRecentes(cargasRecentes)
                .build();
    }

    private DashboardCargaRecenteResponse toCargaRecente(Carga c) {
        Rota r = c.getRota();

        String origem = "N/A";
        String destino = "N/A";

        if (r != null) {
            if (r.getCidadeInicio() != null && !r.getCidadeInicio().isBlank()) {
                origem = r.getCidadeInicio();
            }
            if (r.getCidades() != null && !r.getCidades().isEmpty()) {
                destino = r.getCidades().get(r.getCidades().size() - 1);
            }
        }

        return DashboardCargaRecenteResponse.builder()
                .numeroCarga(c.getNumeroCarga())
                .origem(origem)
                .destino(destino)
                .valorTotal(c.getValorTotal())
                .pesoCarga(c.getPesoCarga())
                .status(c.getStatusCarga() != null ? c.getStatusCarga().name() : null)
                .dtSaida(c.getDtSaida())
                .build();
    }
}
