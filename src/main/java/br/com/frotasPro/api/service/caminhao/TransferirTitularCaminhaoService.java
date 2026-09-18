package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;

/**
 * Troca só o motorista titular do caminhão, sem exigir os demais campos do
 * cadastro (ao contrário do PUT /caminhao/{codigo}, que substitui o caminhão
 * inteiro e trava em campos ainda não preenchidos vindos da sincronização
 * com o WinThor).
 */
@Service
@AllArgsConstructor
public class TransferirTitularCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final MotoristaRepository motoristaRepository;

    @Transactional
    public CaminhaoResponse transferir(String codigoCaminhao, String motoristaTitularCodigo) {
        Caminhao caminhao = caminhaoRepository.findByCodigoAndAtivoTrue(codigoCaminhao)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Caminhão não encontrado: " + codigoCaminhao));

        aplicarTitular(caminhao, motoristaTitularCodigo);

        caminhao = caminhaoRepository.save(caminhao);
        return CaminhaoMapper.toResponse(caminhao);
    }

    /** Também usado pelo AtualizarCaminhaoService (edição completa), pra não duplicar a validação de titular. */
    public void aplicarTitular(Caminhao caminhao, String motoristaTitularCodigo) {
        if (hasText(motoristaTitularCodigo)) {
            Motorista motoristaTitular = motoristaRepository
                    .findByCodigo(motoristaTitularCodigo.trim().toUpperCase())
                    .orElseThrow(() -> new ObjectNotFound(
                            "ERRO: Motorista titular não encontrado: " + motoristaTitularCodigo));

            // Motorista já é titular de outro caminhão? Desvincula de lá em vez
            // de travar com erro — reatribuir titular é justamente pra isso
            // (motorista trocou de caminhão), não faz sentido exigir que o
            // usuário vá desvincular manualmente no outro cadastro antes.
            caminhaoRepository.findByMotoristaTitularId(motoristaTitular.getId())
                    .filter(outroCaminhao -> !outroCaminhao.getId().equals(caminhao.getId()))
                    .ifPresent(outroCaminhao -> {
                        outroCaminhao.setMotoristaTitular(null);
                        // saveAndFlush (não save) de propósito: precisa ir pro
                        // banco JÁ, antes de setar esse motorista como titular
                        // do caminhão atual mais abaixo — senão o Hibernate
                        // pode ordenar os dois UPDATEs ao contrário na hora do
                        // flush e violar a constraint única por um instante
                        // (o motorista apareceria titular dos dois ao mesmo
                        // tempo), estourando DataIntegrityViolationException.
                        caminhaoRepository.saveAndFlush(outroCaminhao);
                    });

            caminhao.setMotoristaTitular(motoristaTitular);
        } else {
            caminhao.setMotoristaTitular(null);
        }
    }
}
