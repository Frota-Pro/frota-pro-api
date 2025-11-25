package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Oficina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OficinaRepository extends JpaRepository<Oficina, UUID> {

    Page<Oficina> findAll(Pageable pageable);

    Optional<Oficina> findByCodigo(String codigo);
}
