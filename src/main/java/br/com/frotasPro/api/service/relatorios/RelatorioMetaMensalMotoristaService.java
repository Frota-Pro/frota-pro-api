package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.LinhaRelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.controller.response.RelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelatorioMetaMensalMotoristaService {

    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final MetaRepository metaRepository;

    @Transactional(readOnly = true)
    public RelatorioMetaMensalMotoristaResponse gerar(
            String codigoMotorista,
            LocalDate inicio,
            LocalDate fim) {

        List<Carga> cargas = cargaRepository
                .findByMotoristaCodigoAndPeriodo(codigoMotorista, inicio, fim);

        if (cargas.isEmpty()) {
            return RelatorioMetaMensalMotoristaResponse.builder()
                    .codigoMotorista(codigoMotorista)
                    .periodoInicio(inicio)
                    .periodoFim(fim)
                    .linhas(new ArrayList<>())
                    .totalKmRodado(0L)
                    .totalTonelada(BigDecimal.ZERO)
                    .totalLitros(BigDecimal.ZERO)
                    .totalValorAbastecimento(BigDecimal.ZERO)
                    .mediaGeralKmPorLitro(BigDecimal.ZERO)
                    .realizadoToneladaPercentual(BigDecimal.ZERO)
                    .build();
        }

        Motorista motorista = cargas.get(0).getMotorista();
        Caminhao caminhao = cargas.get(0).getCaminhao();
        CategoriaCaminhao categoriaCaminhao =
                caminhao != null ? caminhao.getCategoria() : null;

        BigDecimal objetivoMesTonelada = buscarMetaTonelada(motorista, caminhao, categoriaCaminhao);
        BigDecimal metaConsumo = buscarMetaConsumoCombustivel(motorista, caminhao, categoriaCaminhao);

        List<LinhaRelatorioMetaMensalMotoristaResponse> linhas = new ArrayList<>();

        BigDecimal totalTonelada = BigDecimal.ZERO;
        long totalKmRodado = 0L;
        BigDecimal totalLitros = BigDecimal.ZERO;
        BigDecimal totalValorAbastecimento = BigDecimal.ZERO;

        for (Carga carga : cargas) {

            Integer kmIni = carga.getKmInicial();
            Integer kmFin = carga.getKmFinal();
            long kmRodado = (kmIni != null && kmFin != null) ? kmFin - kmIni : 0L;

            List<Abastecimento> abastecimentos =
                    abastecimentoRepository.findByCaminhaoAndKmRodado(
                            carga.getCaminhao().getId(),
                            carga.getKmInicial(),
                            carga.getKmFinal()
                    );

            BigDecimal litros = abastecimentos.stream()
                    .map(Abastecimento::getQtLitros)
                    .filter(l -> l != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal valorAbastecimento = abastecimentos.stream()
                    .map(Abastecimento::getValorTotal)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal mediaKmLitro = calcularMediaKmLitro(kmRodado, litros, metaConsumo);

            LinhaRelatorioMetaMensalMotoristaResponse linha =
                    LinhaRelatorioMetaMensalMotoristaResponse.builder()
                            .data(carga.getDtSaida())               // ajustar se for dtSaida
                            .lote(carga.getNumeroCarga())              // ou outro campo
                            .cidade(carga.getRota().getCidadeInicio())         // ajustar nome
                            .valorCarga(carga.getValorTotal())        // R$ carga
                            .tonelagem(carga.getPesoCarga())           // peso/tonelada
                            .kmInicial(kmIni)
                            .kmFinal(kmFin)
                            .kmRodado(kmRodado)
                            .litros(litros)
                            .valorAbastecimento(valorAbastecimento)
                            .mediaKmLitro(mediaKmLitro)
                            .build();

            linhas.add(linha);


            if (carga.getPesoCarga() != null) {
                totalTonelada = totalTonelada.add(carga.getPesoCarga());
            }
            totalKmRodado += kmRodado;
            totalLitros = totalLitros.add(litros);
            totalValorAbastecimento = totalValorAbastecimento.add(valorAbastecimento);
        }

        BigDecimal mediaGeralKmPorLitro = totalLitros.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(totalKmRodado)
                .divide(totalLitros, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal realizadoPercentual =
                (objetivoMesTonelada != null && objetivoMesTonelada.compareTo(BigDecimal.ZERO) > 0)
                        ? totalTonelada.multiply(BigDecimal.valueOf(100))
                        .divide(objetivoMesTonelada, 2, RoundingMode.HALF_UP)
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

    private BigDecimal calcularMediaKmLitro(long kmRodado,
                                            BigDecimal litros,
                                            BigDecimal metaConsumo) {

        if (litros != null && litros.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(kmRodado)
                    .divide(litros, 2, RoundingMode.HALF_UP);
        }

        // Sem abastecimento na rota:
        // usa a meta como referência (pra ter M/LT em todas as linhas)
        return metaConsumo;
    }

    private BigDecimal buscarMetaTonelada(Motorista motorista,
                                          Caminhao caminhao,
                                          CategoriaCaminhao categoriaCaminhao) {

        // prioridade: motorista > caminhão > categoria
        Optional<Meta> metaOpt = Optional.empty();

        if (motorista != null) {
            metaOpt = metaRepository
                    .findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
                            TipoMeta.TONELADA, motorista, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && caminhao != null) {
            metaOpt = metaRepository
                    .findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
                            TipoMeta.TONELADA, caminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && categoriaCaminhao != null) {
            metaOpt = metaRepository
                    .findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
                            TipoMeta.TONELADA, categoriaCaminhao, StatusMeta.EM_ANDAMENTO);
        }

        return metaOpt.map(Meta::getValorMeta).orElse(null);
    }

    private BigDecimal buscarMetaConsumoCombustivel(Motorista motorista,
                                                    Caminhao caminhao,
                                                    CategoriaCaminhao categoriaCaminhao) {

        Optional<Meta> metaOpt = Optional.empty();

        if (caminhao != null) {
            metaOpt = metaRepository
                    .findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
                            TipoMeta.CONSUMO_COMBUSTIVEL, caminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && categoriaCaminhao != null) {
            metaOpt = metaRepository
                    .findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
                            TipoMeta.CONSUMO_COMBUSTIVEL, categoriaCaminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && motorista != null) {
            metaOpt = metaRepository
                    .findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
                            TipoMeta.CONSUMO_COMBUSTIVEL, motorista, StatusMeta.EM_ANDAMENTO);
        }

        return metaOpt.map(Meta::getValorMeta).orElse(null);
    }
}
