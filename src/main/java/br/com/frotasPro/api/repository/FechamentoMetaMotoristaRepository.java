package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.FechamentoMetaMotorista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FechamentoMetaMotoristaRepository extends JpaRepository<FechamentoMetaMotorista, UUID> {

    Optional<FechamentoMetaMotorista> findByMotoristaIdAndMesReferencia(UUID motoristaId, LocalDate mesReferencia);

    boolean existsByMotoristaIdAndMesReferenciaAndReabertoEmIsNull(UUID motoristaId, LocalDate mesReferencia);

    /** Confirmações ativas (não reabertas) de todos os motoristas num mês. */
    java.util.List<FechamentoMetaMotorista> findByMesReferenciaAndReabertoEmIsNull(LocalDate mesReferencia);
}
