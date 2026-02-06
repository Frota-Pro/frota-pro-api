package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.domain.enums.StatusPneu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PneuRepository extends JpaRepository<Pneu, UUID> {
    Optional<Pneu> findByCodigo(String codigo);

    Page<Pneu> findByStatus(StatusPneu status, Pageable pageable);

    Page<Pneu> findByCodigoContainingIgnoreCaseOrNumeroSerieContainingIgnoreCaseOrMarcaContainingIgnoreCaseOrModeloContainingIgnoreCaseOrMedidaContainingIgnoreCase(
            String c1, String c2, String c3, String c4, String c5,
            Pageable pageable
    );
}
