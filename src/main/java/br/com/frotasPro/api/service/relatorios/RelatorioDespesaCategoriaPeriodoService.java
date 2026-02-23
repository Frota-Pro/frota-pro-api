package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioDespesaCategoriaPeriodoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.DespesaParada;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.TipoDespesa;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.DespesaParadaRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioDespesaCategoriaPeriodoService {

    private static final String CATEGORIA_FROTA = "Frota";
    private static final String CATEGORIA_PESSOAL = "Pessoal";

    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final DespesaParadaRepository despesaParadaRepository;

    public RelatorioDespesaCategoriaPeriodoResponse gerar(LocalDate inicio, LocalDate fim) {
        PeriodoValidator.obrigatorio(inicio, fim, "periodo");

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(LocalTime.MAX);

        List<RelatorioDespesaCategoriaPeriodoResponse.Linha> linhas = new ArrayList<>();

        List<Abastecimento> abastecimentos = abastecimentoRepository.findByPeriodoComFiltro(inicioDt, fimDt, null, null);
        for (Abastecimento a : abastecimentos) {
            String referencia = a.getCaminhao().getPlaca() + " (" + a.getCaminhao().getCodigo() + ")";
            String descricao = "Posto: " + valorOuTraco(a.getPosto()) +
                    " | Combustível: " + (a.getTipoCombustivel() == null ? "-" : a.getTipoCombustivel().name());

            linhas.add(RelatorioDespesaCategoriaPeriodoResponse.Linha.builder()
                    .data(a.getDtAbastecimento().toLocalDate())
                    .categoriaPrincipal(CATEGORIA_FROTA)
                    .subcategoria("Abastecimento")
                    .referencia(referencia)
                    .descricao(descricao)
                    .cidade(valorOuTraco(a.getCidade()))
                    .valor(nvl(a.getValorTotal()))
                    .build());
        }

        List<Manutencao> manutencoes = manutencaoRepository.findByPeriodo(inicio, fim);
        for (Manutencao m : manutencoes) {
            LocalDate dataRef = m.getDataFimManutencao() != null ? m.getDataFimManutencao() : m.getDataInicioManutencao();
            String referencia = m.getCaminhao().getPlaca() + " (" + m.getCaminhao().getCodigo() + ")";
            String descricao = valorOuTraco(m.getDescricao()) + " | Tipo: " +
                    (m.getTipoManutencao() == null ? "-" : m.getTipoManutencao().name());

            linhas.add(RelatorioDespesaCategoriaPeriodoResponse.Linha.builder()
                    .data(dataRef)
                    .categoriaPrincipal(CATEGORIA_FROTA)
                    .subcategoria("Manutenções")
                    .referencia(referencia)
                    .descricao(descricao)
                    .cidade("-")
                    .valor(nvl(m.getValor()))
                    .build());
        }

        List<DespesaParada> despesasParada = despesaParadaRepository.findByPeriodo(inicioDt, fimDt);
        for (DespesaParada d : despesasParada) {
            String subcategoria = mapearSubcategoriaDespesaParada(d.getTipoDespesa());
            if (subcategoria == null) {
                continue;
            }

            String categoriaPrincipal = TipoDespesa.PNEU.equals(d.getTipoDespesa()) ? CATEGORIA_FROTA : CATEGORIA_PESSOAL;
            String referencia = d.getParadaCarga().getCarga().getNumeroCarga() + " | " +
                    d.getParadaCarga().getCarga().getMotorista().getNome();

            linhas.add(RelatorioDespesaCategoriaPeriodoResponse.Linha.builder()
                    .data(d.getDataHora().toLocalDate())
                    .categoriaPrincipal(categoriaPrincipal)
                    .subcategoria(subcategoria)
                    .referencia(referencia)
                    .descricao(valorOuTraco(d.getDescricao()))
                    .cidade(valorOuTraco(d.getCidade()))
                    .valor(nvl(d.getValor()))
                    .build());
        }

        linhas.sort(Comparator
                .comparing(RelatorioDespesaCategoriaPeriodoResponse.Linha::getCategoriaPrincipal, Comparator.nullsLast(String::compareTo))
                .thenComparing(RelatorioDespesaCategoriaPeriodoResponse.Linha::getSubcategoria, Comparator.nullsLast(String::compareTo))
                .thenComparing(RelatorioDespesaCategoriaPeriodoResponse.Linha::getData, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(RelatorioDespesaCategoriaPeriodoResponse.Linha::getReferencia, Comparator.nullsLast(String::compareTo)));

        BigDecimal totalAbastecimento = totalPorSubcategoria(linhas, "Abastecimento");
        BigDecimal totalManutencoes = totalPorSubcategoria(linhas, "Manutenções");
        BigDecimal totalPneu = totalPorSubcategoria(linhas, "Pneu");
        BigDecimal totalAlimentacao = totalPorSubcategoria(linhas, "Alimentação");
        BigDecimal totalPernoite = totalPorSubcategoria(linhas, "Pernoite");

        BigDecimal totalFrota = totalAbastecimento.add(totalManutencoes).add(totalPneu);
        BigDecimal totalPessoal = totalAlimentacao.add(totalPernoite);
        BigDecimal totalGeral = totalFrota.add(totalPessoal);

        return RelatorioDespesaCategoriaPeriodoResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalFrota(totalFrota)
                .totalPessoal(totalPessoal)
                .totalGeral(totalGeral)
                .totalAbastecimento(totalAbastecimento)
                .totalManutencoes(totalManutencoes)
                .totalPneu(totalPneu)
                .totalAlimentacao(totalAlimentacao)
                .totalPernoite(totalPernoite)
                .quantidadeLancamentos(linhas.size())
                .linhas(linhas)
                .build();
    }

    private String mapearSubcategoriaDespesaParada(TipoDespesa tipoDespesa) {
        if (tipoDespesa == null) {
            return null;
        }
        if (TipoDespesa.PNEU.equals(tipoDespesa)) {
            return "Pneu";
        }
        if (TipoDespesa.ALIMENTACAO.equals(tipoDespesa)) {
            return "Alimentação";
        }
        if (TipoDespesa.PERNOITE.equals(tipoDespesa)) {
            return "Pernoite";
        }
        return null;
    }

    private BigDecimal totalPorSubcategoria(List<RelatorioDespesaCategoriaPeriodoResponse.Linha> linhas, String subcategoria) {
        return linhas.stream()
                .filter(l -> subcategoria.equals(l.getSubcategoria()))
                .map(RelatorioDespesaCategoriaPeriodoResponse.Linha::getValor)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String valorOuTraco(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
