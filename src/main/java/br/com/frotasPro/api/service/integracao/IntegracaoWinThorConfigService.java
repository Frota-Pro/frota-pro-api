package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.controller.integracao.dto.IntegracaoWinThorConfigResponse;
import br.com.frotasPro.api.controller.integracao.dto.IntegracaoWinThorConfigUpdateRequest;
import br.com.frotasPro.api.domain.integracao.IntegracaoWinThorConfig;
import br.com.frotasPro.api.repository.integracao.IntegracaoWinThorConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IntegracaoWinThorConfigService {

    private final IntegracaoWinThorConfigRepository repository;

    @Transactional(readOnly = true)
    public IntegracaoWinThorConfig getOrDefault(UUID empresaId) {
        return repository.findByEmpresaId(empresaId).orElse(null);
    }

    @Transactional
    public IntegracaoWinThorConfig ensureExists(UUID empresaId) {
        return repository.findByEmpresaId(empresaId).orElseGet(() -> {
            IntegracaoWinThorConfig cfg = new IntegracaoWinThorConfig();
            cfg.setEmpresaId(empresaId);
            return repository.save(cfg);
        });
    }

    @Transactional
    public IntegracaoWinThorConfigResponse atualizar(UUID empresaId, IntegracaoWinThorConfigUpdateRequest req) {
        IntegracaoWinThorConfig cfg = ensureExists(empresaId);

        if (req.ativo() != null) cfg.setAtivo(req.ativo());
        if (req.intervaloMin() != null) cfg.setIntervaloMin(req.intervaloMin());
        if (req.syncCaminhoes() != null) cfg.setSyncCaminhoes(req.syncCaminhoes());
        if (req.syncMotoristas() != null) cfg.setSyncMotoristas(req.syncMotoristas());
        if (req.syncCargas() != null) cfg.setSyncCargas(req.syncCargas());

        IntegracaoWinThorConfig saved = repository.save(cfg);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IntegracaoWinThorConfigResponse buscar(UUID empresaId) {
        IntegracaoWinThorConfig cfg = ensureExists(empresaId);
        return toResponse(cfg);
    }

    public IntegracaoWinThorConfigResponse toResponse(IntegracaoWinThorConfig cfg) {
        return IntegracaoWinThorConfigResponse.builder()
                .empresaId(cfg.getEmpresaId())
                .ativo(cfg.isAtivo())
                .intervaloMin(cfg.getIntervaloMin())
                .syncCaminhoes(cfg.isSyncCaminhoes())
                .syncMotoristas(cfg.isSyncMotoristas())
                .syncCargas(cfg.isSyncCargas())
                .criadoEm(cfg.getCriadoEm())
                .atualizadoEm(cfg.getAtualizadoEm())
                .build();
    }
}
