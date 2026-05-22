package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CargaTransferencia;
import br.com.frotasPro.api.domain.enums.StatusTransferenciaCarga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CargaTransferenciaRepository extends JpaRepository<CargaTransferencia, UUID> {

    List<CargaTransferencia> findByCargaOrigemIdAndStatus(UUID cargaOrigemId, StatusTransferenciaCarga status);
}
