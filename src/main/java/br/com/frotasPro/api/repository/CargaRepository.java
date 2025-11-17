package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Carga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CargaRepository extends JpaRepository<Carga, UUID> {

    Page<Carga> findAll(Pageable pageable);

    Optional<Carga> findByNumeroCarga(String numeroCarga);

    boolean existsByNumeroCarga(String numeroCarga);
}
