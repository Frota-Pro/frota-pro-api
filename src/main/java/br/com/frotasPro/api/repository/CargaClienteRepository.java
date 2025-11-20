package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CargaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CargaClienteRepository extends JpaRepository<CargaCliente, CargaCliente.CargaClienteId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CargaCliente cc WHERE cc.carga.id = :cargaId")
    void deleteByCargaId(UUID cargaId);
}