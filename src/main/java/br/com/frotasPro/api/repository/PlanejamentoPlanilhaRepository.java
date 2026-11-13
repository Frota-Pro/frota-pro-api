package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PlanejamentoPlanilha;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlanejamentoPlanilhaRepository extends JpaRepository<PlanejamentoPlanilha, UUID> {

    boolean existsByData(LocalDate data);

    List<PlanejamentoPlanilha> findAllByOrderByDataDesc(Pageable pageable);
}
