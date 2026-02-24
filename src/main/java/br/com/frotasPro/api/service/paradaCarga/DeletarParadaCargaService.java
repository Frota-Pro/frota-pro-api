package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarParadaCargaService {

    private final ParadaCargaRepository paradaRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public void deletar(UUID id) {

        ParadaCarga parada = paradaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Parada não encontrada"));

        Carga carga = parada.getCarga();

        if (carga.getKmFinal() != null) {
            throw new BusinessException("Não é possível excluir paradas de uma carga já finalizada.");
        }

        paradaRepository.delete(parada);

        notificacaoService.notificar(
                EventoNotificacao.PARADA_APAGADA,
                TipoNotificacao.ALERTA,
                "Parada removida",
                "Parada " + parada.getId() + " da carga "
                        + (carga != null ? carga.getNumeroCarga() : "N/A")
                        + " foi excluída.",
                "PARADA_CARGA",
                parada.getId(),
                carga != null ? carga.getNumeroCarga() : null
        );
    }
}
