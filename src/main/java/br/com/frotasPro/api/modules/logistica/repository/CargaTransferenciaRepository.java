package br.com.frotasPro.api.modules.logistica.repository;

import br.com.frotasPro.api.modules.logistica.domain.CargaTransferencia;
import br.com.frotasPro.api.shared.enums.StatusTransferenciaCarga;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CargaTransferenciaRepository extends JpaRepository<CargaTransferencia, UUID> {

    List<CargaTransferencia> findByCargaOrigemIdAndStatus(UUID cargaOrigemId, StatusTransferenciaCarga status);
}
