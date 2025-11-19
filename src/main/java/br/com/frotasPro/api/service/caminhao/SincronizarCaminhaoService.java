package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.integracao.dto.CaminhaoSyncResponseEvent;
import br.com.frotasPro.api.integracao.dto.CaminhaoWinThorDto;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

    @Transactional
    public void sincronizar(CaminhaoSyncResponseEvent event) {

        if (event.getCaminhoes() == null || event.getCaminhoes().isEmpty()) {
            log.info("Nenhum caminhão retornado no sync. jobId={}", event.getJobId());
            return;
        }

        event.getCaminhoes().forEach(this::upsert);
    }

    private void upsert(CaminhaoWinThorDto dto) {

        String codigoExterno = String.valueOf(dto.getCodVeiculo());

        Caminhao caminhao = caminhaoRepository.findByCodigoExterno(codigoExterno)
                .orElseGet(Caminhao::new);

        boolean novo = caminhao.getId() == null;

        if (novo) {
            caminhao.setCodigoExterno(codigoExterno);
            caminhao.setAtivo(true);
            caminhao.setStatus(Status.DISPONIVEL);
        }

        caminhao.setPlaca(normalizarPlaca(dto.getPlaca()));
        caminhao.setDescricao(dto.getDescricao());
        caminhao.setMarca(dto.getMarca());

        if (dto.getPesoMaximoKg() != null) {
            caminhao.setMaxPeso(BigDecimal.valueOf(dto.getPesoMaximoKg().doubleValue()));
        }

        caminhaoRepository.save(caminhao);

        log.info("Caminhão {} sincronizado. codigoExterno={} placa={}",
                novo ? "CRIADO" : "ATUALIZADO",
                codigoExterno,
                caminhao.getPlaca());
    }

    private String normalizarPlaca(String placa) {
        if (placa == null) return null;
        return placa.replace("-", "")
                .replace(" ", "")
                .toUpperCase();
    }
}
