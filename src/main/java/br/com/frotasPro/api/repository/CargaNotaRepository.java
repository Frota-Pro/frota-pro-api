package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.CargaNotaId;
import br.com.frotasPro.api.projections.ClienteHistoricoRotaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface CargaNotaRepository extends JpaRepository<CargaNota, CargaNotaId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CargaNota cn WHERE cn.carga.id = :cargaId")
    void deleteByCargaId(UUID cargaId);

    @Query("""
        select cn.cliente as cliente,
               count(distinct cn.carga.id) as quantidadeCargas,
               max(cn.carga.dtSaida) as ultimaCargaEm
        from CargaNota cn
        where cn.carga.rota.codigo = :codigoRota
        group by cn.cliente
        order by count(distinct cn.carga.id) desc, cn.cliente asc
        """)
    List<ClienteHistoricoRotaProjection> buscarClientesHistoricoPorRota(@Param("codigoRota") String codigoRota);
}