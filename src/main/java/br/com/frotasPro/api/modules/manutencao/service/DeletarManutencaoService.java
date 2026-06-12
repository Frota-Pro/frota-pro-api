package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.domain.Manutencao;
import br.com.frotasPro.api.modules.manutencao.repository.ManutencaoRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeletarManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final NotificacaoService notificacaoService;

    public void deletar(String codigo) {
        Manutencao manutencao = manutencaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Manutenção não encontrada para o código: " + codigo));
        String codigoRef = manutencao.getCodigo();
        var idRef = manutencao.getId();
        manutencaoRepository.delete(manutencao);

        notificacaoService.notificar(
                EventoNotificacao.MANUTENCAO_APAGADA,
                TipoNotificacao.ALERTA,
                "Manutenção removida",
                "Manutenção " + codigoRef + " foi excluída.",
                "MANUTENCAO",
                idRef,
                codigoRef
        );
    }
}
