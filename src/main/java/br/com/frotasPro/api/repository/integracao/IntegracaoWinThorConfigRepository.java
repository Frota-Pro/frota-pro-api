package br.com.frotasPro.api.repository.integracao;

import br.com.frotasPro.api.domain.integracao.IntegracaoWinThorConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IntegracaoWinThorConfigRepository extends JpaRepository<IntegracaoWinThorConfig, UUID> {
    Optional<IntegracaoWinThorConfig> findByEmpresaId(UUID empresaId);
}
