package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.LinhaRelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.controller.response.RelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoLinhaRelatorioMotorista;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelatorioMetaMensalMotoristaService {

    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    /**
     * Códigos dos motoristas ativos, na mesma ordem que a tela de Motoristas usa
     * (nome) — usado pelo controller pra gerar o relatório de cada um EM PARALELO
     * (ver RelatorioPdfController.metaMensalTodosMotoristasPdf). Não dá pra
     * paralelizar chamando gerar() daqui de dentro (auto-invocação: o
     * @Transactional de gerar() não pega quando é a própria classe chamando),
     * por isso o controller busca essa lista e chama gerar() ele mesmo, através
     * do bean gerenciado pelo Spring.
     */
    @Transactional(readOnly = true)
    public List<String> listarCodigosMotoristasAtivos() {
        return motoristaRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(Motorista::getCodigo)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioMetaMensalMotoristaResponse gerar(
            String codigoMotorista,
            LocalDate inicio,
            LocalDate fim) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtSaida");

        List<Carga> cargasProprias = cargaRepository
                .findByMotoristaCodigoAndPeriodo(codigoMotorista, inicio, fim);

        // Cargas do caminhão deste motorista (ele é o titular), dirigidas por
        // outro — contam km rodado/km-por-litro pra ele mesmo sem ter dirigido.
        List<Carga> cargasEmprestadas = cargaRepository
                .findByCaminhaoTitularCodigoDirigidaPorOutroNoPeriodo(codigoMotorista, inicio, fim);

        if (cargasProprias.isEmpty() && cargasEmprestadas.isEmpty()) {
            // Motorista pode não ter nenhuma carga iniciada ainda no período (ex: carga
            // só SINCRONIZADA, sem dtSaida), mas a meta do mês já existe e deve aparecer
            // mesmo assim — só o realizado é que fica zerado.
            Motorista motoristaSemCarga = motoristaRepository.findByCodigo(codigoMotorista).orElse(null);
            Caminhao caminhaoSemCarga = motoristaSemCarga != null
                    ? caminhaoRepository.findByMotoristaTitularId(motoristaSemCarga.getId()).orElse(null)
                    : null;
            CategoriaCaminhao categoriaSemCarga = caminhaoSemCarga != null ? caminhaoSemCarga.getCategoria() : null;

            return RelatorioMetaMensalMotoristaResponse.builder()
                    .nomeMotorista(motoristaSemCarga != null ? motoristaSemCarga.getNome() : null)
                    .codigoMotorista(codigoMotorista)
                    .placaCaminhao(caminhaoSemCarga != null ? caminhaoSemCarga.getPlaca() : null)
                    .codigoCaminhao(caminhaoSemCarga != null ? caminhaoSemCarga.getCodigo() : null)
                    .periodoInicio(inicio)
                    .periodoFim(fim)
                    .objetivoMesTonelada(buscarMetaTonelada(motoristaSemCarga, caminhaoSemCarga, categoriaSemCarga))
                    .metaConsumoKmPorLitro(buscarMetaConsumoCombustivel(motoristaSemCarga, caminhaoSemCarga, categoriaSemCarga))
                    .linhas(new ArrayList<>())
                    .totalKmRodado(0L)
                    .totalTonelada(BigDecimal.ZERO)
                    .totalLitros(BigDecimal.ZERO)
                    .totalValorAbastecimento(BigDecimal.ZERO)
                    .mediaGeralKmPorLitro(BigDecimal.ZERO)
                    .realizadoToneladaPercentual(BigDecimal.ZERO)
                    .build();
        }

        Motorista motorista = !cargasProprias.isEmpty()
                ? cargasProprias.get(0).getMotorista()
                : cargasEmprestadas.get(0).getCaminhao().getMotoristaTitular();

        // O objetivo do mês é sempre o do caminhão titular do motorista, quando houver
        // vínculo cadastrado — assim, se ele pegar carga em outro caminhão eventualmente,
        // a meta de referência continua sendo a dele, não a do caminhão emprestado.
        // Sem vínculo cadastrado, cai no comportamento anterior: caminhão da primeira
        // carga própria do período.
        Caminhao caminhao = caminhaoRepository.findByMotoristaTitularId(motorista.getId())
                .orElseGet(() -> !cargasProprias.isEmpty() ? cargasProprias.get(0).getCaminhao() : null);
        CategoriaCaminhao categoriaCaminhao = caminhao != null ? caminhao.getCategoria() : null;

        BigDecimal objetivoMesTonelada = buscarMetaTonelada(motorista, caminhao, categoriaCaminhao);
        BigDecimal metaConsumo = buscarMetaConsumoCombustivel(motorista, caminhao, categoriaCaminhao);

        List<LinhaRelatorioMetaMensalMotoristaResponse> linhas = new ArrayList<>();

        BigDecimal totalTonelada = BigDecimal.ZERO;
        long totalKmRodado = 0L;
        BigDecimal totalLitros = BigDecimal.ZERO;
        BigDecimal totalValorAbastecimento = BigDecimal.ZERO;
        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();

        // Cargas que ELE dirigiu: tonelada sempre conta (foi ele quem entregou).
        // Km rodado/km-por-litro só contam quando o caminhão também é dele —
        // senão, essa economia é responsabilidade do titular do caminhão
        // emprestado (aparece no relatório dele, não neste).
        for (Carga carga : cargasProprias) {
            Motorista titular = carga.getCaminhao() != null ? carga.getCaminhao().getMotoristaTitular() : null;
            TipoLinhaRelatorioMotorista tipo = titular == null
                    ? TipoLinhaRelatorioMotorista.CAMINHAO_SEM_TITULAR
                    : titular.getId().equals(motorista.getId())
                            ? TipoLinhaRelatorioMotorista.PROPRIA
                            : TipoLinhaRelatorioMotorista.CAMINHAO_DE_OUTRO_TITULAR;

            LinhaRelatorioMetaMensalMotoristaResponse linha = construirLinha(carga, integracaoAtiva, tipo, null);
            linhas.add(linha);

            if (carga.getPesoCarga() != null) {
                totalTonelada = totalTonelada.add(carga.getPesoCarga());
            }
            if (tipo == TipoLinhaRelatorioMotorista.PROPRIA) {
                totalKmRodado += linha.getKmRodado();
                totalLitros = totalLitros.add(linha.getLitros());
                totalValorAbastecimento = totalValorAbastecimento.add(linha.getValorAbastecimento());
            }
        }

        // Cargas do caminhão dele, dirigidas por outro motorista: o oposto —
        // km rodado/km-por-litro contam (é o caminhão dele), tonelada não
        // (quem entregou foi o outro motorista, conta no relatório dele).
        for (Carga carga : cargasEmprestadas) {
            String nomeQuemDirigiu = carga.getMotorista() != null ? carga.getMotorista().getNome() : null;
            LinhaRelatorioMetaMensalMotoristaResponse linha = construirLinha(
                    carga, integracaoAtiva, TipoLinhaRelatorioMotorista.MOTORISTA_TERCEIRO_NO_MEU_CAMINHAO, nomeQuemDirigiu);
            linhas.add(linha);

            totalKmRodado += linha.getKmRodado();
            totalLitros = totalLitros.add(linha.getLitros());
            totalValorAbastecimento = totalValorAbastecimento.add(linha.getValorAbastecimento());
        }

        linhas.sort(Comparator.comparing(LinhaRelatorioMetaMensalMotoristaResponse::getData));

        BigDecimal mediaGeralKmPorLitro;
        if (totalLitros.compareTo(BigDecimal.ZERO) > 0) {
            mediaGeralKmPorLitro = BigDecimal.valueOf(totalKmRodado).divide(totalLitros, 2, RoundingMode.HALF_UP);
        } else {
            // Fallback só entre as linhas que realmente contam km/L pra esse
            // motorista (PROPRIA ou MOTORISTA_TERCEIRO_NO_MEU_CAMINHAO) — uma
            // linha CAMINHAO_DE_OUTRO_TITULAR/CAMINHAO_SEM_TITULAR não pode
            // "vazar" média de um caminhão que não conta pra ele.
            mediaGeralKmPorLitro = linhas.stream()
                    .filter(l -> l.getTipoLinha() == TipoLinhaRelatorioMotorista.PROPRIA
                            || l.getTipoLinha() == TipoLinhaRelatorioMotorista.MOTORISTA_TERCEIRO_NO_MEU_CAMINHAO)
                    .map(LinhaRelatorioMetaMensalMotoristaResponse::getMediaKmLitro)
                    .filter(m -> m != null)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }

        BigDecimal realizadoPercentual =
                (objetivoMesTonelada != null && objetivoMesTonelada.compareTo(BigDecimal.ZERO) > 0)
                        ? totalTonelada.multiply(BigDecimal.valueOf(100)).divide(objetivoMesTonelada, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        return RelatorioMetaMensalMotoristaResponse.builder()
                .nomeMotorista(motorista.getNome())
                .codigoMotorista(motorista.getCodigo())
                .placaCaminhao(caminhao != null ? caminhao.getPlaca() : null)
                .codigoCaminhao(caminhao != null ? caminhao.getCodigo() : null)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .objetivoMesTonelada(objetivoMesTonelada)
                .metaConsumoKmPorLitro(metaConsumo)
                .linhas(linhas)
                .totalTonelada(totalTonelada)
                .totalKmRodado(totalKmRodado)
                .totalLitros(totalLitros)
                .totalValorAbastecimento(totalValorAbastecimento)
                .mediaGeralKmPorLitro(mediaGeralKmPorLitro)
                .realizadoToneladaPercentual(realizadoPercentual)
                .build();
    }

    private LinhaRelatorioMetaMensalMotoristaResponse construirLinha(
            Carga carga, boolean integracaoAtiva, TipoLinhaRelatorioMotorista tipo, String motoristaQueDirigiu) {

        Integer kmIni = carga.getKmInicial();
        Integer kmFin = carga.getKmFinal();
        long kmRodado = (kmIni != null && kmFin != null) ? kmFin - kmIni : 0L;

        List<Abastecimento> abastecimentos = abastecimentoRepository.findByCargaId(carga.getId());

        BigDecimal litros = abastecimentos.stream()
                .map(Abastecimento::getQtLitros)
                .filter(l -> l != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorAbastecimento = abastecimentos.stream()
                .map(Abastecimento::getValorTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal mediaKmLitro = calcularMediaKmLitro(kmRodado, litros, null);

        return LinhaRelatorioMetaMensalMotoristaResponse.builder()
                .data(carga.getDtSaida())
                .lote(CargaMapper.resolverNumeroExibicao(carga.getNumeroCarga(), carga.getNumeroCargaExterno(), integracaoAtiva))
                .cidade(carga.getRota().getCidadeInicio())
                .valorCarga(carga.getValorTotal())
                .tonelagem(carga.getPesoCarga())
                .kmInicial(kmIni)
                .kmFinal(kmFin)
                .kmRodado(kmRodado)
                .litros(litros)
                .valorAbastecimento(valorAbastecimento)
                .mediaKmLitro(mediaKmLitro)
                .tipoLinha(tipo)
                .motoristaQueDirigiu(motoristaQueDirigiu)
                .build();
    }

    private BigDecimal calcularMediaKmLitro(long kmRodado,
                                            BigDecimal litros,
                                            BigDecimal mediaReferencia) {
        if (litros != null && litros.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(kmRodado).divide(litros, 2, RoundingMode.HALF_UP);
        }
        return mediaReferencia;
    }

    private BigDecimal buscarMetaTonelada(Motorista motorista, Caminhao caminhao, CategoriaCaminhao categoriaCaminhao) {

        Optional<Meta> metaOpt = Optional.empty();

        if (motorista != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.TONELADA, motorista, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && caminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.TONELADA, caminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && categoriaCaminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.TONELADA, categoriaCaminhao, StatusMeta.EM_ANDAMENTO);
        }

        return metaOpt.map(Meta::getValorMeta).orElse(null);
    }

    private BigDecimal buscarMetaConsumoCombustivel(Motorista motorista, Caminhao caminhao, CategoriaCaminhao categoriaCaminhao) {

        Optional<Meta> metaOpt = Optional.empty();

        if (motorista != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.CONSUMO_COMBUSTIVEL, motorista, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && caminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.CONSUMO_COMBUSTIVEL, caminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && categoriaCaminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.CONSUMO_COMBUSTIVEL, categoriaCaminhao, StatusMeta.EM_ANDAMENTO);
        }

        return metaOpt.map(Meta::getValorMeta).orElse(null);
    }
}
