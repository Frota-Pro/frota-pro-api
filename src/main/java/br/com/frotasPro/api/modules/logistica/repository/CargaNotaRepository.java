package br.com.frotasPro.api.modules.logistica.repository;

import br.com.frotasPro.api.modules.logistica.domain.CargaNota;
import br.com.frotasPro.api.modules.logistica.domain.CargaNotaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface CargaNotaRepository extends JpaRepository<CargaNota, CargaNotaId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CargaNota cn WHERE cn.carga.id = :cargaId")
    void deleteByCargaId(UUID cargaId);
}