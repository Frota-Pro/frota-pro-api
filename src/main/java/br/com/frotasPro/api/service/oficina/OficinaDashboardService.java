package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.response.*;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.OficinaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OficinaDashboardService {

    private final OficinaRepository oficinaRepository;
    private final ManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public OficinaDashboardResponse gerar(String codigoOficina, LocalDate inicio, LocalDate fim) {

        PeriodoValidator.obrigatorio(inicio, fim, "dataInicioManutencao");

        var oficina = oficinaRepository.findByCodigo(codigoOficina)
                .orElseThrow(() -> new ObjectNotFound("Oficina não encontrada: " + codigoOficina));

        BigDecimal total = manutencaoRepository.sumValorByOficinaAndPeriodo(codigoOficina, inicio, fim);
        Long qtd = manutencaoRepository.countByOficinaAndPeriodo(codigoOficina, inicio, fim);

        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (qtd != null && qtd > 0) {
            ticketMedio = total.divide(BigDecimal.valueOf(qtd), 2, RoundingMode.HALF_UP);
        }

        Long agendadas = manutencaoRepository.countByOficinaAndStatusAndPeriodo(codigoOficina, StatusManutencao.AGENDADA, inicio, fim);
        Long emAndamento = manutencaoRepository.countByOficinaAndStatusAndPeriodo(codigoOficina, StatusManutencao.EM_ANDAMENTO, inicio, fim);
        Long concluidas = manutencaoRepository.countByOficinaAndStatusAndPeriodo(codigoOficina, StatusManutencao.CONCLUIDA, inicio, fim);
        Long canceladas = manutencaoRepository.countByOficinaAndStatusAndPeriodo(codigoOficina, StatusManutencao.CANCELADA, inicio, fim);

        BigDecimal totalPecas = manutencaoRepository.sumItensByTipoAndOficina(codigoOficina, "PECA", inicio, fim);
        BigDecimal totalServicos = manutencaoRepository.sumItensByTipoAndOficina(codigoOficina, "SERVICO", inicio, fim);

        List<SerieMensalValorResponse> serie = manutencaoRepository.serieMensalByOficina(codigoOficina, inicio, fim)
                .stream()
                .map(p -> SerieMensalValorResponse.builder()
                        .mes(String.format("%04d-%02d", p.getAno(), p.getMes()))
                        .total(p.getTotal() == null ? BigDecimal.ZERO : p.getTotal())
                        .build())
                .toList();

        List<TopCaminhaoCustoResponse> top = manutencaoRepository.topCaminhoesByOficina(codigoOficina, inicio, fim, 10)
                .stream()
                .map(p -> TopCaminhaoCustoResponse.builder()
                        .codigoCaminhao(p.getCodigoCaminhao())
                        .descricaoCaminhao(p.getDescricaoCaminhao())
                        .total(p.getTotal() == null ? BigDecimal.ZERO : p.getTotal())
                        .build())
                .toList();

        return OficinaDashboardResponse.builder()
                .codigoOficina(oficina.getCodigo())
                .nomeOficina(oficina.getNome())
                .inicio(inicio)
                .fim(fim)

                .totalOrcamentos(total == null ? BigDecimal.ZERO : total)
                .qtdManutencoes(qtd == null ? 0 : qtd)
                .ticketMedio(ticketMedio)

                .qtdAgendadas(agendadas == null ? 0 : agendadas)
                .qtdEmAndamento(emAndamento == null ? 0 : emAndamento)
                .qtdConcluidas(concluidas == null ? 0 : concluidas)
                .qtdCanceladas(canceladas == null ? 0 : canceladas)

                .totalPecas(totalPecas == null ? BigDecimal.ZERO : totalPecas)
                .totalServicos(totalServicos == null ? BigDecimal.ZERO : totalServicos)

                .serieMensal(serie)
                .topCaminhoes(top)
                .build();
    }
}
