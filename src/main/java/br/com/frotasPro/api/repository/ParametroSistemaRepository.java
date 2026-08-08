package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, UUID> {
    Optional<ParametroSistema> findByEmpresaId(UUID empresaId);
}
