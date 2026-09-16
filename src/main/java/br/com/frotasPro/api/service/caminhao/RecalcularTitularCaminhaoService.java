package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.RecalcularTitularCaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Correção manual, sob demanda — diferente da atribuição automática de
 * titular (congelada na criação de cada carga/abastecimento, ver
 * Carga.titularNoPeriodo/Abastecimento.titularNoPeriodo), que protege
 * relatórios de meses já fechados de uma troca de titular no meio do
 * caminho. Usada quando o titular só está sendo cadastrado agora, mas o
 * motorista já dirigia esse caminhão desde antes (cadastro tardio) — o
 * admin escolhe a partir de qual data corrigir.
 */
@Service
@AllArgsConstructor
public class RecalcularTitularCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;

    @Transactional
    public RecalcularTitularCaminhaoResponse recalcular(String codigoCaminhao, LocalDate dataInicio) {
        if (dataInicio == null) {
            throw new BusinessException("Informe a data a partir da qual recalcular.");
        }

        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigoCaminhao)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigoCaminhao));

        Motorista titularAtual = caminhao.getMotoristaTitular();
        if (titularAtual == null) {
            throw new BusinessException("Este caminhão não tem motorista titular cadastrado.");
        }

        int cargasAtualizadas = cargaRepository.atualizarTitularNoPeriodoPorCaminhaoAPartirDe(
                caminhao.getId(), dataInicio, titularAtual);

        int abastecimentosAtualizados = abastecimentoRepository.atualizarTitularNoPeriodoPorCaminhaoAPartirDe(
                caminhao.getId(), dataInicio.atStartOfDay(), titularAtual);

        return RecalcularTitularCaminhaoResponse.builder()
                .codigoCaminhao(caminhao.getCodigo())
                .motoristaTitularCodigo(titularAtual.getCodigo())
                .motoristaTitularNome(titularAtual.getNome())
                .dataInicio(dataInicio)
                .cargasAtualizadas(cargasAtualizadas)
                .abastecimentosAtualizados(abastecimentosAtualizados)
                .build();
    }
}
