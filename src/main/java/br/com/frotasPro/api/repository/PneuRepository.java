package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Pneu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PneuRepository extends JpaRepository <Pneu, UUID> {

    Optional<Pneu> findByCodigo(String codigo);

    Page<Pneu> findByEixoId(UUID eixoId, Pageable pageable);

    Page<Pneu> findByEixoCaminhaoCodigo(String codigoCaminhao, Pageable pageable);
}
