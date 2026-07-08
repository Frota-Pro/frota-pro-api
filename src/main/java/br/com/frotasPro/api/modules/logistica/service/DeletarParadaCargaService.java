package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.domain.ParadaCarga;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import br.com.frotasPro.api.shared.exception.BusinessException;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
