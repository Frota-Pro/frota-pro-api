package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.ConfiguracaoEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoEmpresaRepository extends JpaRepository<ConfiguracaoEmpresa, UUID> {

    Optional<ConfiguracaoEmpresa> findByEmpresaId(UUID empresaId);
}
