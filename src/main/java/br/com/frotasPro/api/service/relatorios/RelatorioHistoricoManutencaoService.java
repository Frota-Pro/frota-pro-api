package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioHistoricoManutencaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioHistoricoManutencaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final ManutencaoRepository manutencaoRepository;

    public RelatorioHistoricoManutencaoResponse gerar(String codigoCaminhao, LocalDate inicio, LocalDate fim) {

        Caminhao caminhao = caminhaoRepository.findByCodigo(codigoCaminhao)
                .orElseThrow(() -> new IllegalArgumentException("Caminhão não encontrado: " + codigoCaminhao));

        List<Manutencao> itens = manutencaoRepository.findByCaminhaoAndPeriodo(caminhao.getId(), inicio, fim);

        List<RelatorioHistoricoManutencaoResponse.Linha> linhas = itens.stream()
                .map(m -> RelatorioHistoricoManutencaoResponse.Linha.builder()
                        .data(m.getDataFimManutencao())
                        .descricao(m.getDescricao())
                        .tipo(m.getTipoManutencao() == null ? null : m.getTipoManutencao().name())
                        .valor(nvl(m.getValor()))
                        .status(m.getStatusManutencao() == null ? null : m.getStatusManutencao().name())
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = itens.stream().map(Manutencao::getValor).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);

        return RelatorioHistoricoManutencaoResponse.builder()
                .codigoCaminhao(caminhao.getCodigo())
                .placaCaminhao(caminhao.getPlaca())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalManutencao(total)
                .linhas(linhas)
                .build();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
