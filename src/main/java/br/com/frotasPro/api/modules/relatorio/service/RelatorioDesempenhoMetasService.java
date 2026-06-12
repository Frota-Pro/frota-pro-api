package br.com.frotasPro.api.modules.relatorio.service;

import br.com.frotasPro.api.modules.meta.dto.response.RelatorioDesempenhoMetasResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.meta.domain.MetaCategoriaCaminhaoVinculo;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.modules.meta.repository.MetaCategoriaCaminhaoVinculoRepository;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.modules.meta.service.MetaResultadoService;
import br.com.frotasPro.api.modules.meta.service.MetaProgressoService;
import br.com.frotasPro.api.shared.enums.TipoMeta;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioDesempenhoMetasService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final CategoriaCaminhaoRepository categoriaRepository;
    private final MetaCategoriaCaminhaoVinculoRepository vinculoRepository;
    private final MetaProgressoService metaProgressoService;
    private final MetaResultadoService metaResultadoService;

    @Transactional
    public RelatorioDesempenhoMetasResponse gerar(LocalDate inicio,
                                                  LocalDate fim,
                                                  TipoMeta tipoMeta,
                                                  String caminhaoFiltro,
                                                  String motoristaFiltro,
                                                  String categoriaFiltro) {
        PeriodoValidator.obrigatorio(inicio, fim, "periodo");
        validarFiltros(caminhaoFiltro, motoristaFiltro, categoriaFiltro);

        Caminhao caminhaoRef = buscarCaminhao(caminhaoFiltro);
        Motorista motoristaRef = buscarMotorista(motoristaFiltro);
        String categoriaCodigoRef = normalizar(categoriaFiltro);
        if (categoriaCodigoRef != null) {
            categoriaRepository.findByCodigo(categoriaCodigoRef)
                    .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada: " + categoriaFiltro));
        }

        List<Meta> metas = metaRepository.buscarMetasParaDesempenho(inicio, fim, tipoMeta);
        List<RelatorioDesempenhoMetasResponse.Linha> linhas = new ArrayList<>();

        for (Meta meta : metas) {
            LocalDate calculoInicio = maiorData(inicio, meta.getDataIncio());
            LocalDate calculoFim = menorData(fim, meta.getDataFim());
            if (calculoInicio == null || calculoFim == null || calculoInicio.isAfter(calculoFim)) {
                continue;
            }

            if (meta.getCaminhao() != null) {
                if (motoristaRef != null || categoriaCodigoRef != null) {
                    continue;
                }
                if (caminhaoRef != null && !caminhaoRef.getId().equals(meta.getCaminhao().getId())) {
                    continue;
                }
                linhas.add(montarLinhaCaminhao(meta, meta.getCaminhao(), "CAMINHAO", calculoInicio, calculoFim));
                continue;
            }

            if (meta.getMotorista() != null) {
                if (caminhaoRef != null || categoriaCodigoRef != null) {
                    continue;
                }
                if (motoristaRef != null && !motoristaRef.getId().equals(meta.getMotorista().getId())) {
                    continue;
                }
                linhas.add(montarLinhaMotorista(meta, meta.getMotorista(), calculoInicio, calculoFim));
                continue;
            }

            if (meta.getCategoria() != null) {
                if (motoristaRef != null) {
                    continue;
                }
                if (categoriaCodigoRef != null && !categoriaCodigoRef.equals(meta.getCategoria().getCodigo())) {
                    continue;
                }
                List<Caminhao> caminhoes = caminhoesDaMetaCategoria(meta);
                for (Caminhao caminhao : caminhoes) {
                    if (caminhaoRef != null && !caminhaoRef.getId().equals(caminhao.getId())) {
                        continue;
                    }
                    linhas.add(montarLinhaCaminhao(meta, caminhao, "CATEGORIA", calculoInicio, calculoFim));
                }
            }
        }

        linhas.sort(Comparator
                .comparing(RelatorioDesempenhoMetasResponse.Linha::getAlvoTipo, Comparator.nullsLast(String::compareTo))
                .thenComparing(l -> valorOrdenacao(l.getCaminhaoCodigo(), l.getMotoristaCodigo()))
                .thenComparing(RelatorioDesempenhoMetasResponse.Linha::getTipoMeta));

        long totalDentro = linhas.stream().filter(l -> Boolean.TRUE.equals(l.getMetaAtingida())).count();
        long totalLinhas = linhas.size();
        long totalFora = totalLinhas - totalDentro;
        BigDecimal percentualSucesso = totalLinhas == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalDentro)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLinhas), 2, RoundingMode.HALF_UP);

        return RelatorioDesempenhoMetasResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .tipoMeta(tipoMeta)
                .filtroCaminhao(caminhaoFiltro)
                .filtroMotorista(motoristaFiltro)
                .filtroCategoria(categoriaFiltro)
                .totalLinhas(totalLinhas)
                .totalDentroMeta(totalDentro)
                .totalForaMeta(totalFora)
                .percentualSucesso(percentualSucesso)
                .linhas(linhas)
                .build();
    }

    private RelatorioDesempenhoMetasResponse.Linha montarLinhaCaminhao(Meta meta, Caminhao caminhao, String origem,
                                                                       LocalDate inicio, LocalDate fim) {
        BigDecimal realizado = metaProgressoService.calcularValorRealizado(meta, caminhao, null, inicio, fim);
        BigDecimal percentual = metaProgressoService.calcularPercentual(realizado, meta.getValorMeta());
        Boolean atingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), realizado, meta.getValorMeta());
        metaResultadoService.registrar(meta, caminhao, realizado, percentual, atingida, inicio, fim);

        return baseBuilder(meta, realizado, percentual, atingida, inicio, fim)
                .alvoTipo("CAMINHAO")
                .origemMeta(origem)
                .origemMetaDescricao("CATEGORIA".equals(origem)
                        ? "Meta herdada da categoria " + meta.getCategoria().getCodigo()
                        : "Meta direta do caminhão")
                .caminhaoCodigo(caminhao.getCodigo())
                .caminhaoDescricao(caminhao.getDescricao())
                .categoriaCodigo(meta.getCategoria() != null ? meta.getCategoria().getCodigo() : null)
                .categoriaDescricao(meta.getCategoria() != null ? meta.getCategoria().getDescricao() : null)
                .build();
    }

    private RelatorioDesempenhoMetasResponse.Linha montarLinhaMotorista(Meta meta, Motorista motorista,
                                                                        LocalDate inicio, LocalDate fim) {
        BigDecimal realizado = metaProgressoService.calcularValorRealizado(meta, null, motorista, inicio, fim);
        BigDecimal percentual = metaProgressoService.calcularPercentual(realizado, meta.getValorMeta());
        Boolean atingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), realizado, meta.getValorMeta());

        return baseBuilder(meta, realizado, percentual, atingida, inicio, fim)
                .alvoTipo("MOTORISTA")
                .origemMeta("MOTORISTA")
                .origemMetaDescricao("Meta direta do motorista")
                .motoristaCodigo(motorista.getCodigo())
                .motoristaNome(motorista.getNome())
                .build();
    }

    private RelatorioDesempenhoMetasResponse.Linha.LinhaBuilder baseBuilder(Meta meta,
                                                                            BigDecimal realizado,
                                                                            BigDecimal percentual,
                                                                            Boolean atingida,
                                                                            LocalDate inicio,
                                                                            LocalDate fim) {
        return RelatorioDesempenhoMetasResponse.Linha.builder()
                .metaId(meta.getId())
                .tipoMeta(meta.getTipoMeta())
                .descricaoMeta(meta.getDescricao())
                .regraAtingimento(meta.getTipoMeta().getRegraAtingimento())
                .regraAtingimentoTexto(regraTexto(meta.getTipoMeta().getRegraAtingimento()))
                .valorMeta(meta.getValorMeta())
                .valorRealizado(realizado)
                .percentual(percentual)
                .unidade(meta.getUnidade())
                .metaAtingida(atingida)
                .status(metaProgressoService.statusDesempenho(realizado, atingida))
                .periodoCalculoInicio(inicio)
                .periodoCalculoFim(fim);
    }

    private List<Caminhao> caminhoesDaMetaCategoria(Meta meta) {
        List<MetaCategoriaCaminhaoVinculo> vinculos = vinculoRepository.findByMetaId(meta.getId());
        if (!vinculos.isEmpty()) {
            return vinculos.stream()
                    .map(MetaCategoriaCaminhaoVinculo::getCaminhao)
                    .filter(caminhao -> caminhao != null && caminhao.isAtivo())
                    .toList();
        }
        return caminhaoRepository.findByCategoriaIdAndAtivoTrue(meta.getCategoria().getId());
    }

    private void validarFiltros(String caminhao, String motorista, String categoria) {
        int count = (normalizar(caminhao) != null ? 1 : 0)
                + (normalizar(motorista) != null ? 1 : 0)
                + (normalizar(categoria) != null ? 1 : 0);
        if (count > 1) {
            throw new IllegalArgumentException("Informe apenas um filtro de alvo: caminhao OU motorista OU categoria.");
        }
    }

    private Caminhao buscarCaminhao(String filtro) {
        String valor = normalizar(filtro);
        if (valor == null) {
            return null;
        }
        return caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(valor)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado: " + filtro));
    }

    private Motorista buscarMotorista(String filtro) {
        String valor = normalizar(filtro);
        if (valor == null) {
            return null;
        }
        return motoristaRepository.findByCodigoOuCodigoExternoOuNome(valor)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado: " + filtro));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }

    private LocalDate maiorData(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private LocalDate menorData(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    private String regraTexto(String regra) {
        if ("MENOR_OU_IGUAL".equals(regra)) {
            return "Menor ou igual a meta";
        }
        if ("MAIOR_OU_IGUAL".equals(regra)) {
            return "Maior ou igual a meta";
        }
        return regra;
    }

    private String valorOrdenacao(String caminhao, String motorista) {
        if (caminhao != null) {
            return caminhao;
        }
        if (motorista != null) {
            return motorista;
        }
        return "";
    }
}
