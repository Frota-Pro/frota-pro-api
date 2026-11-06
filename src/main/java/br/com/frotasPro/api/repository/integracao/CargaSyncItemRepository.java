package br.com.frotasPro.api.repository.integracao;

import br.com.frotasPro.api.domain.integracao.CargaSyncItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CargaSyncItemRepository extends JpaRepository<CargaSyncItem, UUID> {

    List<CargaSyncItem> findByJobIdOrderByNumeroCargaExternoAsc(UUID jobId);

    List<CargaSyncItem> findByNumeroCargaExternoOrderByCriadoEmDesc(String numeroCargaExterno, Pageable pageable);

    @Modifying
    @Query("delete from CargaSyncItem i where i.criadoEm < :corte")
    int deletarAnterioresA(@Param("corte") LocalDateTime corte);
}
