package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.MovimentacaoSemCarga;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.modules.logistica.repository.MovimentacaoSemCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RegistrarMovimentacaoSemCargaService {

    private final MovimentacaoSemCargaRepository movimentacaoRepository;
    private final AbastecimentoRepository abastecimentoRepository;

    public void registrarSeHouver(Carga carga, Integer kmInicial) {
        Caminhao caminhao = carga.getCaminhao();
        if (caminhao == null || caminhao.getOdometroUltimaCarga() == null || kmInicial == null) {
            return;
        }

        Integer kmOrigem = caminhao.getOdometroUltimaCarga();
        if (kmInicial <= kmOrigem) {
            return;
        }

        int kmRodado = kmInicial - kmOrigem;
        BigDecimal mediaKmLitro = abastecimentoRepository.mediaKmLitroPonderadaPorCaminhao(caminhao.getId());
        BigDecimal valorLitroMedio = abastecimentoRepository.valorLitroMedioPorCaminhao(caminhao.getId());
        BigDecimal custoEstimado = calcularCustoEstimado(kmRodado, mediaKmLitro, valorLitroMedio);

        MovimentacaoSemCarga movimentacao = new MovimentacaoSemCarga();
        movimentacao.setCaminhao(caminhao);
        movimentacao.setCargaInicio(carga);
        movimentacao.setDataMovimentacao(LocalDate.now());
        movimentacao.setKmOrigem(kmOrigem);
        movimentacao.setKmDestino(kmInicial);
        movimentacao.setKmRodado(kmRodado);
        movimentacao.setMediaKmLitroUsada(mediaKmLitro);
        movimentacao.setValorLitroMedio(valorLitroMedio);
        movimentacao.setCustoEstimado(custoEstimado);

        movimentacaoRepository.save(movimentacao);
    }

    private BigDecimal calcularCustoEstimado(int kmRodado, BigDecimal mediaKmLitro, BigDecimal valorLitroMedio) {
        if (mediaKmLitro == null || valorLitroMedio == null || mediaKmLitro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal litrosEstimados = BigDecimal.valueOf(kmRodado).divide(mediaKmLitro, 6, RoundingMode.HALF_UP);
        return litrosEstimados.multiply(valorLitroMedio).setScale(2, RoundingMode.HALF_UP);
    }
}
