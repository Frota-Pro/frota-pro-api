package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioVidaUtilPneuResponse;
import br.com.frotasPro.api.controller.response.VidaUtilPneuResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.domain.PneuInstalacaoAtual;
import br.com.frotasPro.api.domain.TrocaPneuManutencao;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.PneuInstalacaoAtualRepository;
import br.com.frotasPro.api.repository.PneuMovimentacaoRepository;
import br.com.frotasPro.api.repository.PneuRepository;
import br.com.frotasPro.api.repository.TrocaPneuManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioVidaUtilPneuService {

    private static final String FILTRO_TODOS = "Todos";
    private static final String SEM_CAMINHAO = "-";

    private final TrocaPneuManutencaoRepository trocaRepo;
    private final PneuRepository pneuRepository;
    private final PneuInstalacaoAtualRepository instalacaoRepository;
    private final PneuMovimentacaoRepository movimentacaoRepository;
    private final CaminhaoRepository caminhaoRepository;

    public RelatorioVidaUtilPneuResponse gerar(String codigoCaminhao, String codigoPneu) {
        String filtroPneu = normalizar(codigoPneu);
        String filtroCaminhao = normalizar(codigoCaminhao);

        Optional<Caminhao> caminhaoFiltro = Optional.empty();
        if (filtroCaminhao != null) {
            caminhaoFiltro = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(filtroCaminhao);
            if (caminhaoFiltro.isEmpty()) {
                return RelatorioVidaUtilPneuResponse.builder()
                        .filtroCaminhao(filtroCaminhao)
                        .filtroPneu(filtroPneu == null ? FILTRO_TODOS : filtroPneu)
                        .totalPneus(0L)
                        .linhas(List.of())
                        .build();
            }
        }

        List<Pneu> pneus = carregarPneus(filtroPneu);
        if (pneus.isEmpty()) {
            return RelatorioVidaUtilPneuResponse.builder()
                    .filtroCaminhao(descricaoCaminhaoFiltro(caminhaoFiltro))
                    .filtroPneu(filtroPneu == null ? FILTRO_TODOS : filtroPneu)
                    .totalPneus(0L)
                    .linhas(List.of())
                    .build();
        }

        Map<UUID, PneuInstalacaoAtual> instalacaoPorPneu = carregarInstalacoes(caminhaoFiltro);
        Map<UUID, Caminhao> caminhaoPorId = carregarCaminhoes(instalacaoPorPneu);

        List<RelatorioVidaUtilPneuResponse.Linha> linhas = new ArrayList<>();
        for (Pneu pneu : pneus) {
            PneuInstalacaoAtual instalacao = instalacaoPorPneu.get(pneu.getId());

            BigDecimal kmEmUsoAtual = kmEmUsoAtual(pneu, instalacao);
            BigDecimal kmTotal = nvl(pneu.getKmTotalAcumulado()).add(kmEmUsoAtual);
            BigDecimal kmMeta = nvl(pneu.getKmMetaAtual());

            BigDecimal percentualVida = BigDecimal.ZERO;
            if (kmMeta.compareTo(BigDecimal.ZERO) > 0) {
                percentualVida = kmTotal
                        .divide(kmMeta, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            String caminhao = SEM_CAMINHAO;
            if (instalacao != null) {
                Caminhao c = caminhaoPorId.get(instalacao.getCaminhaoId());
                if (c != null) {
                    caminhao = c.getPlaca() != null && !c.getPlaca().isBlank() ? c.getPlaca() : c.getCodigo();
                }
            }

            linhas.add(RelatorioVidaUtilPneuResponse.Linha.builder()
                    .codigoPneu(pneu.getCodigo())
                    .numeroSerie(pneu.getNumeroSerie())
                    .marca(pneu.getMarca())
                    .modelo(pneu.getModelo())
                    .medida(pneu.getMedida())
                    .status(pneu.getStatus().name())
                    .caminhao(caminhao)
                    .kmTotal(kmTotal)
                    .kmMeta(kmMeta)
                    .percentualVida(percentualVida)
                    .build());
        }

        linhas.sort(Comparator.comparing(RelatorioVidaUtilPneuResponse.Linha::getCodigoPneu, Comparator.nullsLast(String::compareTo)));

        return RelatorioVidaUtilPneuResponse.builder()
                .filtroCaminhao(descricaoCaminhaoFiltro(caminhaoFiltro))
                .filtroPneu(filtroPneu == null ? FILTRO_TODOS : filtroPneu)
                .totalPneus((long) linhas.size())
                .linhas(linhas)
                .build();
    }

    public List<VidaUtilPneuResponse> listarVidaUtilPorPneu(
            String codigoCaminhao,
            String codigoPneu
    ) {

        List<TrocaPneuManutencao> historico =
                trocaRepo.buscarHistoricoPneus(codigoCaminhao, codigoPneu);

        historico.sort(Comparator
                .comparing((TrocaPneuManutencao t) -> t.getPneu().getCodigo())
                .thenComparing(TrocaPneuManutencao::getKmOdometro));

        List<VidaUtilPneuResponse> resultado = new ArrayList<>();

        String pneuAtual = null;
        TrocaPneuManutencao ultimaInstalacao = null;

        for (TrocaPneuManutencao troca : historico) {

            String codigoPneuAtual = troca.getPneu().getCodigo();

            if (!codigoPneuAtual.equals(pneuAtual)) {
                pneuAtual = codigoPneuAtual;
                ultimaInstalacao = null;
            }

            switch (troca.getTipoTroca()) {
                case INSTALACAO -> {

                    ultimaInstalacao = troca;
                }
                case REMOCAO -> {
                    if (ultimaInstalacao != null) {
                        int kmRodados = troca.getKmOdometro() - ultimaInstalacao.getKmOdometro();

                        resultado.add(
                                VidaUtilPneuResponse.builder()
                                        .codigoPneu(codigoPneuAtual)
                                        .codigoCaminhao(troca.getEixo().getCaminhao().getCodigo())
                                        .eixoNumero(troca.getEixo().getNumero())
                                        .lado(troca.getLado().name())
                                        .posicao(troca.getPosicao().name())
                                        .kmInstalacao(ultimaInstalacao.getKmOdometro())
                                        .kmRemocao(troca.getKmOdometro())
                                        .kmRodados(kmRodados)
                                        .build()
                        );
                        ultimaInstalacao = null;
                    }
                }
                case RODIZIO -> {
                    // aqui dá pra tratar mudança de posição se quiser
                    // por enquanto, pode ignorar ou tratar como REMOCAO+INSTALACAO
                }
            }
        }

        return resultado;
    }

    private List<Pneu> carregarPneus(String codigoPneu) {
        if (codigoPneu == null) {
            return pneuRepository.findAll();
        }
        return pneuRepository.findByCodigo(codigoPneu).map(List::of).orElse(List.of());
    }

    private Map<UUID, PneuInstalacaoAtual> carregarInstalacoes(Optional<Caminhao> caminhaoFiltro) {
        List<PneuInstalacaoAtual> instalacoes;
        if (caminhaoFiltro.isPresent()) {
            instalacoes = instalacaoRepository.findAllByCaminhaoId(caminhaoFiltro.get().getId());
        } else {
            instalacoes = instalacaoRepository.findAll();
        }

        Map<UUID, PneuInstalacaoAtual> porPneu = new HashMap<>();
        for (PneuInstalacaoAtual instalacao : instalacoes) {
            porPneu.put(instalacao.getPneu().getId(), instalacao);
        }
        return porPneu;
    }

    private Map<UUID, Caminhao> carregarCaminhoes(Map<UUID, PneuInstalacaoAtual> instalacaoPorPneu) {
        List<UUID> ids = instalacaoPorPneu.values().stream().map(PneuInstalacaoAtual::getCaminhaoId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Caminhao> map = new HashMap<>();
        for (Caminhao caminhao : caminhaoRepository.findAllById(ids)) {
            map.put(caminhao.getId(), caminhao);
        }
        return map;
    }

    private BigDecimal kmEmUsoAtual(Pneu pneu, PneuInstalacaoAtual instalacao) {
        if (instalacao == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal kmAtual = movimentacaoRepository.findTopByPneu_CodigoOrderByDataEventoDesc(pneu.getCodigo())
                .map(mov -> mov.getKmEvento())
                .orElse(instalacao.getKmInstalacao());
        BigDecimal kmEmUso = nvl(kmAtual).subtract(nvl(instalacao.getKmInstalacao()));
        return kmEmUso.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : kmEmUso;
    }

    private String descricaoCaminhaoFiltro(Optional<Caminhao> caminhaoFiltro) {
        if (caminhaoFiltro.isEmpty()) {
            return FILTRO_TODOS;
        }
        Caminhao caminhao = caminhaoFiltro.get();
        String placa = caminhao.getPlaca() == null ? "" : caminhao.getPlaca();
        return caminhao.getCodigo() + (placa.isBlank() ? "" : " - " + placa);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String v = valor.trim();
        return v.isEmpty() ? null : v;
    }

    private BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
