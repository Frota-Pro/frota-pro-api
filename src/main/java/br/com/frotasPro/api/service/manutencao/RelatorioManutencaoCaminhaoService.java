package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.RelatorioManutencaoCaminhaoResponse;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class RelatorioManutencaoCaminhaoService {

    private final ManutencaoRepository manutencaoRepository;

    public RelatorioManutencaoCaminhaoResponse gerar(
            String codigoCaminhao,
            LocalDate inicio,
            LocalDate fim
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dataInicioManutencao");

        List<Manutencao> manutencoes =
                manutencaoRepository.findAllByCaminhaoCodigoAndDataInicioManutencaoBetween(
                        codigoCaminhao, inicio, fim
                );

        if (manutencoes.isEmpty()) {
            throw new ObjectNotFound("Nenhuma manutenção encontrada para o caminhão e período informados.");
        }

        long total = manutencoes.size();
        long preventivas = manutencoes.stream()
                .filter(m -> m.getTipoManutencao() == TipoManutencao.PREVENTIVA)
                .count();

        long corretivas = manutencoes.stream()
                .filter(m -> m.getTipoManutencao() == TipoManutencao.CORRETIVA)
                .count();

        BigDecimal valorTotal = manutencoes.stream()
                .map(m -> m.getValor() != null ? m.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorMedio = total > 0
                ? valorTotal.divide(BigDecimal.valueOf(total), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        Manutencao primeira = manutencoes.get(0);

        return RelatorioManutencaoCaminhaoResponse.builder()
                .codigoCaminhao(primeira.getCaminhao().getCodigo())
                .caminhao(primeira.getCaminhao().getDescricao())
                .dataInicio(inicio)
                .dataFim(fim)
                .quantidadeManutencoes(total)
                .quantidadePreventiva(preventivas)
                .quantidadeCorretiva(corretivas)
                .valorTotal(valorTotal)
                .valorMedio(valorMedio)
                .build();
    }
}
