package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.integracao.dto.MotoristaSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.MotoristaWinThorDto;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @Transactional
    public void sincronizar(MotoristaSyncResponseEvent event) {

        log.info("Sincronizando {} motoristas (jobId={})",
                event.getMotoristas().size(), event.getJobId());

        event.getMotoristas().forEach(this::upsert);
    }

    private void upsert(MotoristaWinThorDto dto) {

        String codigoExterno = String.valueOf(dto.getCodigoExterno());

        Motorista motorista = motoristaRepository
                .findByCodigoExterno(codigoExterno)
                .orElseGet(Motorista::new);

        boolean novo = motorista.getId() == null;

        if (novo) {
            motorista.setCodigoExterno(codigoExterno);
            motorista.setAtivo(true);
            motorista.setStatus(Status.DISPONIVEL);
        }
        motorista.setNome(dto.getNome());

        motoristaRepository.save(motorista);

        log.info("Motorista sincronizado. codigoExterno={} nome={} novo={}",
                codigoExterno, motorista.getNome(), novo);
    }
}
