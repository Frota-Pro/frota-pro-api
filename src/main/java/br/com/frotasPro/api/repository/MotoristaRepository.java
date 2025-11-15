package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Motorista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MotoristaRepository extends JpaRepository<Motorista, UUID> {
    Page<Motorista> findByAtivoTrue(Pageable pageable);

    Optional<Motorista> findByCodigoAndAtivoTrue(String codigo);

    Optional<Motorista> findByCodigo(String codigo);
}
