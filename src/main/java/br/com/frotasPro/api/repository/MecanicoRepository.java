package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Mecanico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MecanicoRepository extends JpaRepository<Mecanico, UUID> {

    Page<Mecanico> findAll(Pageable pageable);

    Optional<Mecanico> findByCodigo(String codigo);

    Page<Mecanico> findByOficina_Codigo(String codigoOficina, Pageable pageable);

}
