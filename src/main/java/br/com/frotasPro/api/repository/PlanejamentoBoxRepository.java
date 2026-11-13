package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PlanejamentoBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanejamentoBoxRepository extends JpaRepository<PlanejamentoBox, UUID> {

    List<PlanejamentoBox> findByAtivoTrueOrderByNumero();

    Optional<PlanejamentoBox> findByNumeroAndAtivoTrue(Integer numero);
}
