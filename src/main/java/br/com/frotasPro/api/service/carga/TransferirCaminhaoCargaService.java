package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.request.TransferirCaminhaoCargaRequest;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.util.CalcularMediaKmLitroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Corrige o caminhão de uma carga quando o MDF-e foi emitido no WinThor com o
 * veículo errado (e não dá pra reemitir). Marca a carga como "definida
 * manualmente" pra que o próximo sync do WinThor não desfaça a correção, refaz o
 * titular congelado da carga e leva junto o que foi lançado nela: abastecimentos
 * (com a média km/L recalculada no caminhão certo) e manutenções das paradas.
 *
 * Não mexe no odômetro dos caminhões nem recalcula as metas: isso é feito à parte
 * (ajuste do último KM e "processar metas agora").
 */
@Service
@RequiredArgsConstructor
public class TransferirCaminhaoCargaService {

    private final CargaRepository cargaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final CalcularMediaKmLitroService calcularMediaKmLitroService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional
    public CargaResponse transferir(String numeroCarga, TransferirCaminhaoCargaRequest request) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));

        String codigoCaminhao = request.getCodigoCaminhao().trim();
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigoCaminhao)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado: " + codigoCaminhao));

        carga.setCaminhao(caminhao);
        carga.setTitularNoPeriodo(caminhao.getMotoristaTitular());
        carga.setCaminhaoDefinidoManualmente(true);

        moverLancamentosDaCarga(carga, caminhao);

        Carga cargaSalva = cargaRepository.save(carga);

        CargaResponse response = CargaMapper.toResponse(cargaSalva);
        CargaMapper.aplicarNumeroExibicao(response, cargaSalva, integracaoWinThorConfigService.isCargaIntegracaoAtiva());
        return response;
    }

    private void moverLancamentosDaCarga(Carga carga, Caminhao caminhao) {
        List<Abastecimento> abastecimentos = abastecimentoRepository.findByCargaId(carga.getId());
        for (Abastecimento abastecimento : abastecimentos) {
            abastecimento.setCaminhao(caminhao);
            abastecimento.setTitularNoPeriodo(caminhao.getMotoristaTitular());
            // em ordem de data; o select da média enxerga os já movidos (flush automático)
            abastecimento.setMediaKmLitro(calcularMediaKmLitroService.calcular(
                    caminhao,
                    abastecimento.getId(),
                    abastecimento.getDtAbastecimento(),
                    abastecimento.getKmOdometro(),
                    abastecimento.getQtLitros()
            ));
            abastecimentoRepository.save(abastecimento);
        }

        if (carga.getParadas() != null) {
            carga.getParadas().forEach(parada -> {
                if (parada.getManutencoes() != null) {
                    parada.getManutencoes().forEach(m -> m.setCaminhao(caminhao));
                }
            });
        }
    }
}
