package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CategoriaCaminhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaCaminhaoRepository extends JpaRepository<CategoriaCaminhao, UUID> {

    Optional<CategoriaCaminhao> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
