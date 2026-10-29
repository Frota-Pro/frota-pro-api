package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Rota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RotaRepository extends JpaRepository<Rota, UUID> {

    Page<Rota> findAll(Pageable pageable);

    Optional<Rota> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    Optional<Rota> findByCidadeInicio(String cidadeInicio);

}
