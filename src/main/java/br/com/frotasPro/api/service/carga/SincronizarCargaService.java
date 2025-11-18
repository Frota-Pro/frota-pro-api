package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SincronizarCargaService {

    public void sincronizarCargasWinThor(CargaSyncResponseEvent event) {
        log.info("Sincronizando {} cargas da data {}",
                event.getTotalCargas(), event.getDataReferencia());

        event.getCargas().forEach(c -> {
            log.info("Carga numMdfe={} destino={} clientes={}",
                    c.getNumMdfe(), c.getDestino(), c.getTotalClientes());
        });

        // depois aqui entra o upsert no banco
    }

}
