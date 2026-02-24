package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
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
