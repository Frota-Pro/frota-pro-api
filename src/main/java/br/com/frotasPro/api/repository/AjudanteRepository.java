package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Ajudante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AjudanteRepository extends JpaRepository<Ajudante, UUID> {

    Page<Ajudante> findByAtivoTrue(Pageable pageable);

    Optional<Ajudante> findByCodigoAndAtivoTrue(String id);

    boolean existsByCodigoAndAtivoTrue(String codigo);
}
