package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface MetaRepository extends JpaRepository <Meta, UUID>{

    Optional<Meta> findFirstByCaminhaoCodigoAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
            String caminhaoCodigo,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    Optional<Meta> findFirstByCategoriaCodigoAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
            String categoriaCodigo,
            LocalDate dataInicio,
            LocalDate dataFim
    );
}
