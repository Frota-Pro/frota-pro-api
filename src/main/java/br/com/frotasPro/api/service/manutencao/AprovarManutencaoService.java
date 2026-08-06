package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.request.AprovarManutencaoRequest;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusAprovacaoManutencao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;

/**
 * Decide o orçamento de uma manutenção (aprovar ou rejeitar). Separado do
 * fluxo geral de edição porque é uma ação de negócio própria (só quem
 * aprova orçamento deveria poder fazer isso).
 */
@Service
@RequiredArgsConstructor
public class AprovarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final NotificacaoService notificacaoService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional
    public ManutencaoResponse aprovar(String codigo, AprovarManutencaoRequest request) {
        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada para o código: " + codigo));

        if (request.getStatusAprovacao() == StatusAprovacaoManutencao.PENDENTE) {
            throw new BusinessException("Informe se o orçamento foi aprovado ou rejeitado.");
        }

        if (manutencao.getStatusAprovacao() != StatusAprovacaoManutencao.PENDENTE) {
            throw new BusinessException(
                    "O orçamento desta manutenção já foi decidido (" + manutencao.getStatusAprovacao() + "). "
                            + "Edite o valor orçado se precisar reabrir a decisão.");
        }

        manutencao.setStatusAprovacao(request.getStatusAprovacao());
        manutencao.setObservacaoAprovacao(request.getObservacao());
        manutencaoRepository.save(manutencao);

        boolean aprovado = request.getStatusAprovacao() == StatusAprovacaoManutencao.APROVADO;
        String codigoCaminhao = manutencao.getCaminhao() != null ? manutencao.getCaminhao().getCodigo() : "N/A";

        notificacaoService.notificar(
                aprovado ? EventoNotificacao.MANUTENCAO_ORCAMENTO_APROVADO : EventoNotificacao.MANUTENCAO_ORCAMENTO_REJEITADO,
                aprovado ? TipoNotificacao.SUCESSO : TipoNotificacao.ALERTA,
                aprovado ? "Orçamento aprovado" : "Orçamento rejeitado",
                "O orçamento da manutenção " + manutencao.getCodigo() + " do caminhão " + codigoCaminhao
                        + " foi " + (aprovado ? "aprovado" : "rejeitado") + ".",
                "MANUTENCAO",
                manutencao.getId(),
                manutencao.getCodigo()
        );

        return toResponse(manutencao, integracaoWinThorConfigService.isCargaIntegracaoAtiva());
    }
}
