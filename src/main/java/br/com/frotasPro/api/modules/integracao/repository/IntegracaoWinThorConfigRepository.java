package br.com.frotasPro.api.modules.integracao.repository;

import br.com.frotasPro.api.modules.integracao.domain.IntegracaoWinThorConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IntegracaoWinThorConfigRepository extends JpaRepository<IntegracaoWinThorConfig, UUID> {
    Optional<IntegracaoWinThorConfig> findByEmpresaId(UUID empresaId);
}
